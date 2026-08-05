import { useMemo, useState } from 'react';
import { Button, Input, Radio, Select } from 'antd';
import {
  PlusOutlined,
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
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState<'all' | 'running' | 'completed'>('all');
  const [sortBy, setSortBy] = useState<'createdAt' | 'status'>('createdAt');
  const running = useMemo(createRunningTask, []);
  const allTasks = useMemo(() => [running, ...tasks], [running, tasks]);
  const filteredTasks = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    return allTasks
      .filter((task) => !keyword || task.name.toLowerCase().includes(keyword) || task.id.toLowerCase().includes(keyword))
      .filter((task) => status === 'all' || task.status === status)
      .sort((a, b) => sortBy === 'createdAt'
        ? b.createdAt.localeCompare(a.createdAt)
        : a.status.localeCompare(b.status));
  }, [allTasks, search, sortBy, status]);

  return (
    <div className="page-stack">
      <section className="surface-card table-card">
        <div className="section-head">
          <div><h3>全部任务</h3><p>统一查看执行中与历史测评任务</p></div>
          <Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>创建测评</Button>
        </div>
        <div className="filter-bar">
          <Input prefix={<SearchOutlined />} value={search} onChange={(event) => setSearch(event.target.value)} placeholder="搜索任务名称或编号" allowClear className="search-input" />
          <Radio.Group
            value={status}
            onChange={(event) => setStatus(event.target.value)}
            optionType="button"
            buttonStyle="solid"
            options={[{ label: '全部', value: 'all' }, { label: '执行中', value: 'running' }, { label: '已完成', value: 'completed' }]}
          />
          <Select value={sortBy} onChange={setSortBy} options={[{ value: 'createdAt', label: '按创建时间' }, { value: 'status', label: '按任务状态' }]} />
          <span className="filter-count">共 {filteredTasks.length} 个任务</span>
        </div>
        <TaskTable tasks={filteredTasks} onOpenTask={onOpenTask} />
      </section>
    </div>
  );
}
