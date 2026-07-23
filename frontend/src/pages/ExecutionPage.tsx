import { Button, Input, Progress, Radio, Select, Space } from 'antd';
import {
  ArrowRightOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { CASES } from '../mock/data';
import type { EvaluationTask } from '../types';
import { TaskTable } from '../components/tasks/TaskTable';

function createRunningTask(): EvaluationTask {
  return {
    id: 'T-20260723-04',
    name: 'Pi Agent_Pro-3.0_202607231022_赵启铭',
    agentId: 'pi-agent',
    agentVersionId: 'pi-v231',
    modelId: 'm-pro',
    creator: '赵启铭',
    createdAt: '2026-07-23 10:22',
    status: 'running',
    scoringStatus: 'idle',
    runs: CASES.slice(0, 5).map((item, index) => ({
      caseId: item.id,
      status: index < 2 ? 'success' : index === 2 ? 'running' : 'queued',
      durationMs: index < 2 ? 156000 + index * 53000 : 0,
      tokensIn: index < 2 ? 18240 + index * 5200 : 0,
      tokensOut: index < 2 ? 4460 + index * 860 : 0,
    })),
  };
}

export function ExecutionPage({ tasks, onOpenTask, onCreate }: {
  tasks: EvaluationTask[];
  onOpenTask: (task: EvaluationTask) => void;
  onCreate: () => void;
}) {
  const running = createRunningTask();
  const allTasks = [running, ...tasks];

  return (
    <div className="page-stack">
      <section className="running-banner">
        <div className="running-pulse"><span /></div>
        <div className="running-copy">
          <small>当前执行中</small>
          <h3>{running.name}</h3>
          <p>正在执行：{CASES[2].name} · 已完成 2 / 5</p>
        </div>
        <div className="running-progress">
          <div><span>整体进度</span><b>40%</b></div>
          <Progress percent={40} showInfo={false} strokeColor="#fff" trailColor="rgba(255,255,255,.18)" />
        </div>
        <Button ghost onClick={() => onOpenTask(running)}>进入监控 <ArrowRightOutlined /></Button>
      </section>
      <section className="surface-card table-card">
        <div className="section-head">
          <div><h3>全部任务</h3><p>统一查看执行队列与评分进度</p></div>
          <Space>
            <Button icon={<ReloadOutlined />}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>创建测评</Button>
          </Space>
        </div>
        <div className="filter-bar">
          <Input prefix={<SearchOutlined />} placeholder="搜索任务" className="search-input" />
          <Radio.Group
            defaultValue="all"
            optionType="button"
            buttonStyle="solid"
            options={[{ label: '全部', value: 'all' }, { label: '执行中', value: 'running' }, { label: '已完成', value: 'done' }]}
          />
          <Select defaultValue="createdAt" options={[{ value: 'createdAt', label: '按创建时间' }, { value: 'status', label: '按任务状态' }]} />
        </div>
        <TaskTable tasks={allTasks} onOpenTask={onOpenTask} />
      </section>
    </div>
  );
}
