import { useState } from 'react';
import { Button, Input, Select } from 'antd';
import {
  BarChartOutlined,
  CheckCircleFilled,
  ClockCircleOutlined,
  ExportOutlined,
  HistoryOutlined,
  SearchOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { AGENTS } from '../mock/data';
import type { EvaluationTask } from '../types';
import { MetricCard } from '../components/common/MetricCard';
import { TaskTable } from '../components/tasks/TaskTable';

export function RecordsPage({ tasks, onOpenTask }: { tasks: EvaluationTask[]; onOpenTask: (task: EvaluationTask) => void }) {
  const [search, setSearch] = useState('');
  const filtered = tasks.filter((task) => task.name.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="page-stack">
      <section className="metrics-grid records-metrics">
        <MetricCard label="全部记录" value={tasks.length + 21} note="系统历史累计" tone="green" icon={<HistoryOutlined />} />
        <MetricCard label="已完成" value={tasks.filter((task) => task.status === 'completed').length + 17} note="完成率 91.7%" tone="blue" icon={<CheckCircleFilled />} />
        <MetricCard label="执行中" value="2" note="7 条案例正在排队" tone="orange" icon={<ThunderboltOutlined />} />
        <MetricCard label="平均得分" value="84.6" note="最近 30 天" tone="purple" icon={<BarChartOutlined />} />
      </section>
      <section className="surface-card table-card">
        <div className="section-head">
          <div><h3>历史测评</h3><p>按创建时间倒序排列，可复用配置重新发起</p></div>
          <Button icon={<ExportOutlined />}>导出记录</Button>
        </div>
        <div className="filter-bar">
          <Input prefix={<SearchOutlined />} value={search} onChange={(event) => setSearch(event.target.value)} placeholder="搜索任务名称" allowClear className="search-input" />
          <Select defaultValue="all" options={[{ value: 'all', label: '全部 Agent' }, ...AGENTS.map((agent) => ({ value: agent.id, label: agent.name }))]} className="filter-select" />
          <Select defaultValue="all" options={[{ value: 'all', label: '全部状态' }, { value: 'completed', label: '已完成' }, { value: 'running', label: '执行中' }]} className="filter-select" />
          <Button icon={<ClockCircleOutlined />}>时间范围</Button>
          <span className="filter-count">共 {filtered.length + 21} 条记录</span>
        </div>
        <TaskTable tasks={filtered} onOpenTask={onOpenTask} />
      </section>
    </div>
  );
}
