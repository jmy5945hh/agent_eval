import React, { createContext, useContext, useMemo, useRef, useState, useCallback } from 'react';
import {
  AGENTS, MODELS, CASES, CATEGORIES, SCORING_STANDARDS,
  currentStandard, genTrajectory, genError, seedTasks,
} from './mock/data';
import { fmtCompact, caseScore } from './util';

const StoreCtx = createContext(null);

const rand = (min, max) => min + Math.random() * (max - min);

let taskSeq = 100;

export function StoreProvider({ children }) {
  const [user, setUser] = useState(null);
  const [agents] = useState(AGENTS);
  const [models, setModels] = useState(MODELS);
  const [cases, setCases] = useState(CASES);
  const [categories, setCategories] = useState(CATEGORIES);
  const [standards, setStandards] = useState(SCORING_STANDARDS);
  const [tasks, setTasks] = useState(seedTasks);

  const stateRef = useRef({ tasks, cases, standards, models });
  stateRef.current = { tasks, cases, standards, models };
  const timers = useRef({});

  const patchTask = useCallback((taskId, fn) => {
    setTasks((prev) =>
      prev.map((t) => (t.id === taskId ? fn(structuredClone(t)) : t)),
    );
  }, []);

  const getTask = useCallback((taskId) => stateRef.current.tasks.find((t) => t.id === taskId), []);

  // ---------- 执行引擎（一期：串行队列） ----------
  const scheduleNext = useCallback((taskId) => {
    const task = getTask(taskId);
    if (!task || task.status !== 'running') return;
    if (task.runs.some((r) => r.status === 'running')) return;
    const next = task.runs.find((r) => r.status === 'queued');
    if (!next) {
      // 全部结束 → 进入结果处理阶段
      patchTask(taskId, (t) => {
        if (t.phase === 'executing') t.phase = 'executed';
        return t;
      });
      return;
    }
    patchTask(taskId, (t) => {
      const r = t.runs.find((x) => x.caseId === next.caseId);
      r.status = 'running';
      r.startedAt = Date.now();
      t.phase = 'executing';
      return t;
    });
    const timer = setTimeout(() => finishRun(taskId, next.caseId), rand(3500, 7500));
    timers.current[`${taskId}:${next.caseId}`] = timer;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [getTask, patchTask]);

  const finishRun = useCallback((taskId, caseId) => {
    delete timers.current[`${taskId}:${caseId}`];
    const task = getTask(taskId);
    if (!task) return;
    const c = stateRef.current.cases.find((x) => x.id === caseId);
    const ok = Math.random() < 0.72;
    patchTask(taskId, (t) => {
      const r = t.runs.find((x) => x.caseId === caseId);
      if (!r || r.status !== 'running') return t;
      r.status = ok ? 'success' : 'failed';
      r.durationMs = Date.now() - (r.startedAt || Date.now());
      delete r.startedAt;
      r.rounds = Math.floor(rand(4, 12));
      r.tokensIn = Math.floor(rand(12000, 45000));
      r.tokensOut = Math.floor(rand(2500, 12000));
      r.trajectory = genTrajectory(c, ok);
      r.error = ok ? null : genError();
      r.score = null;
      return t;
    });
    scheduleNext(taskId);
  }, [getTask, patchTask, scheduleNext]);

  // ---------- 任务生命周期 ----------
  const launchTask = useCallback(({ name, agentId, modelId, caseIds }) => {
    const { tasks: cur } = stateRef.current;
    const agent = AGENTS.find((a) => a.id === agentId);
    const model = stateRef.current.models.find((m) => m.id === modelId);
    const auto = `${agent.name}_${model.name}_${fmtCompact(new Date())}_${stateRef.current.userName || 'user'}`;
    const id = `T-${fmtCompact(new Date())}-${++taskSeq}`;
    const task = {
      id,
      name: name?.trim() || auto,
      agentId, modelId,
      creator: stateRef.current.userName || 'user',
      createdAt: new Date().toISOString(),
      status: 'running', // running | completed | cancelled
      phase: 'executing', // executing | executed | done
      scoringModelId: null,
      standardVersion: null,
      scoringStatus: 'idle', // idle | scoring | scored | confirmed
      runs: caseIds.map((cid) => ({
        caseId: cid, status: 'queued', attempts: 0, removed: false, removeReason: '',
        rounds: null, tokensIn: null, tokensOut: null, durationMs: null,
        error: null, trajectory: null, score: null,
      })),
    };
    setTasks((prev) => [task, ...prev]);
    setTimeout(() => scheduleNext(id), 600);
    return id;
  }, [scheduleNext]);

  const cancelTask = useCallback((taskId) => {
    Object.keys(timers.current).forEach((k) => {
      if (k.startsWith(taskId + ':')) {
        clearTimeout(timers.current[k]);
        delete timers.current[k];
      }
    });
    patchTask(taskId, (t) => {
      t.status = 'cancelled';
      t.runs.forEach((r) => {
        if (r.status === 'queued' || r.status === 'running') r.status = 'cancelled';
        delete r.startedAt;
      });
      return t;
    });
  }, [patchTask]);

  const cancelRun = useCallback((taskId, caseId) => {
    patchTask(taskId, (t) => {
      const r = t.runs.find((x) => x.caseId === caseId);
      if (r && r.status === 'queued') r.status = 'cancelled';
      return t;
    });
  }, [patchTask]);

  const rerunCase = useCallback((taskId, caseId) => {
    patchTask(taskId, (t) => {
      const r = t.runs.find((x) => x.caseId === caseId);
      if (!r) return t;
      Object.assign(r, {
        status: 'queued', attempts: (r.attempts || 0) + 1,
        rounds: null, tokensIn: null, tokensOut: null, durationMs: null,
        error: null, trajectory: null, score: null,
      });
      if (t.status === 'running') t.phase = 'executing';
      return t;
    });
    setTimeout(() => scheduleNext(taskId), 300);
  }, [patchTask, scheduleNext]);

  const addCasesToTask = useCallback((taskId, caseIds) => {
    patchTask(taskId, (t) => {
      caseIds.forEach((cid) => {
        if (!t.runs.some((r) => r.caseId === cid)) {
          t.runs.push({
            caseId: cid, status: 'queued', attempts: 0, removed: false, removeReason: '',
            rounds: null, tokensIn: null, tokensOut: null, durationMs: null,
            error: null, trajectory: null, score: null,
          });
        }
      });
      if (t.status === 'running') t.phase = 'executing';
      return t;
    });
    setTimeout(() => scheduleNext(taskId), 300);
  }, [patchTask, scheduleNext]);

  const removeRun = useCallback((taskId, caseId, reason) => {
    patchTask(taskId, (t) => {
      const r = t.runs.find((x) => x.caseId === caseId);
      if (r) { r.removed = true; r.removeReason = reason || ''; }
      return t;
    });
  }, [patchTask]);

  const restoreRun = useCallback((taskId, caseId) => {
    patchTask(taskId, (t) => {
      const r = t.runs.find((x) => x.caseId === caseId);
      if (r) { r.removed = false; r.removeReason = ''; }
      return t;
    });
  }, [patchTask]);

  // ---------- 评分 ----------
  const scoreNext = useCallback((taskId) => {
    const task = getTask(taskId);
    if (!task || task.scoringStatus !== 'scoring') return;
    const std = currentStandard(stateRef.current.standards);
    const next = task.runs.find((r) => r.scoring === 'queued');
    if (!next) {
      patchTask(taskId, (t) => {
        t.scoringStatus = 'scored';
        t.runs.forEach((r) => delete r.scoring);
        return t;
      });
      return;
    }
    patchTask(taskId, (t) => {
      t.runs.find((r) => r.caseId === next.caseId).scoring = 'running';
      return t;
    });
    setTimeout(() => {
      const c = stateRef.current.cases.find((x) => x.id === next.caseId);
      patchTask(taskId, (t) => {
        const r = t.runs.find((x) => x.caseId === next.caseId);
        if (!r) return t;
        const ok = r.status === 'success';
        const base = ok ? rand(62, 95) : rand(8, 38);
        const dims = {};
        const comments = {};
        std.dimensions.forEach((d) => {
          const v = Math.max(0, Math.min(100, Math.round(base + rand(-8, 8))));
          dims[d.key] = v;
          comments[d.key] = ok
            ? `${d.label}表现${v >= 85 ? '优秀' : v >= 70 ? '良好' : '一般'}：${d.desc}方面${v >= 70 ? '整体达标，细节可继续打磨' : '存在明显短板，建议针对性优化'}。`
            : `执行未成功，${d.label}按失败基准评定；建议修复问题后重跑再评分。`;
        });
        r.score = {
          dims, comments,
          analysis: ok
            ? `该案例「${c?.name || ''}」实现方案整体合理，主要需求点均已覆盖。建议关注边界条件与异常路径的补充验证。`
            : `案例「${c?.name || ''}」执行失败（${r.error?.category || '未知原因'}），未形成有效产出，本次按失败案例评分。`,
          note: '', edited: false,
          model: stateRef.current.models.find((m) => m.id === t.scoringModelId)?.name || 'Judge-Pro',
          standardVersion: std.version,
        };
        r.scoring = 'queued';
        delete r.scoring;
        return t;
      });
      scoreNext(taskId);
    }, rand(1400, 2600));
  }, [getTask, patchTask]);

  const startScoring = useCallback((taskId, scoringModelId) => {
    const std = currentStandard(stateRef.current.standards);
    patchTask(taskId, (t) => {
      t.scoringModelId = scoringModelId;
      t.standardVersion = std.version;
      t.scoringStatus = 'scoring';
      t.runs.forEach((r) => {
        const eligible = !r.removed && (r.status === 'success' || r.status === 'failed');
        if (eligible) { r.scoring = 'queued'; r.score = null; }
      });
      return t;
    });
    setTimeout(() => scoreNext(taskId), 400);
  }, [patchTask, scoreNext]);

  const updateScore = useCallback((taskId, caseId, dims, note) => {
    patchTask(taskId, (t) => {
      const r = t.runs.find((x) => x.caseId === caseId);
      if (r?.score) {
        r.score.dims = { ...r.score.dims, ...dims };
        if (note != null) r.score.note = note;
        r.score.edited = true;
      }
      return t;
    });
  }, [patchTask]);

  const confirmTask = useCallback((taskId) => {
    patchTask(taskId, (t) => {
      t.status = 'completed';
      t.phase = 'done';
      t.scoringStatus = 'confirmed';
      return t;
    });
  }, [patchTask]);

  // ---------- 案例 CRUD ----------
  const saveCase = useCallback((values, editingId) => {
    setCases((prev) => {
      if (editingId) {
        return prev.map((c) => {
          if (c.id !== editingId) return c;
          const changed = c.prompt !== values.prompt
            || JSON.stringify(c.standardAnswer) !== JSON.stringify(values.standardAnswer);
          return { ...c, ...values, version: changed ? c.version + 1 : c.version };
        });
      }
      const id = `C${String(Date.now()).slice(-6)}`;
      return [{ ...values, id, code: values.code || `NEW-${id.slice(-3)}`, createdAt: new Date().toISOString().slice(0, 16).replace('T', ' ') }, ...prev];
    });
  }, []);

  const deleteCase = useCallback((caseId) => setCases((prev) => prev.filter((c) => c.id !== caseId)), []);

  const addCategory = useCallback((name) => {
    setCategories((prev) => (prev.includes(name) ? prev : [...prev, name]));
  }, []);

  // ---------- 模型 CRUD ----------
  const saveModel = useCallback((values, editingId) => {
    setModels((prev) => editingId
      ? prev.map((m) => (m.id === editingId ? { ...m, ...values } : m))
      : [{ ...values, id: `m-${Date.now()}` }, ...prev]);
  }, []);

  const deleteModel = useCallback((id) => setModels((prev) => prev.filter((m) => m.id !== id)), []);

  // ---------- 评分标准 ----------
  const addStandard = useCallback((std) => {
    setStandards((prev) => [
      ...prev.map((s) => ({ ...s, current: false })),
      { ...std, id: `std-${Date.now()}`, current: true, updatedAt: new Date().toISOString().slice(0, 16).replace('T', ' ') },
    ]);
  }, []);

  const login = useCallback((name) => {
    stateRef.current.userName = name;
    setUser({ name });
  }, []);
  const logout = useCallback(() => setUser(null), []);

  const value = useMemo(() => ({
    user, agents, models, cases, categories, standards, tasks,
    login, logout,
    launchTask, cancelTask, cancelRun, rerunCase, addCasesToTask, removeRun, restoreRun,
    startScoring, updateScore, confirmTask,
    saveCase, deleteCase, addCategory,
    saveModel, deleteModel, addStandard,
    getTask,
  }), [user, agents, models, cases, categories, standards, tasks,
    login, logout, launchTask, cancelTask, cancelRun, rerunCase, addCasesToTask,
    removeRun, restoreRun, startScoring, updateScore, confirmTask,
    saveCase, deleteCase, addCategory, saveModel, deleteModel, addStandard, getTask]);

  return <StoreCtx.Provider value={value}>{children}</StoreCtx.Provider>;
}

export const useStore = () => useContext(StoreCtx);

export { caseScore };
