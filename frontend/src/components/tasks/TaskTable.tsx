import { Button, Table } from 'antd';
import { ArrowRightOutlined, ExperimentOutlined } from '@ant-design/icons';
import { AGENTS, MODELS } from '../../mock/data';
import type { EvaluationTask } from '../../types';
import { ScoreStatusTag, TaskStatusTag } from '../StatusTag';

interface TaskTableProps {
  tasks: EvaluationTask[];
  onOpenTask: (task: EvaluationTask) => void;
  compact?: boolean;
}

export function TaskTable({ tasks, onOpenTask, compact = false }: TaskTableProps) {
  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      render: (_: string, task: EvaluationTask) => (
        <button className="task-link" onClick={() => onOpenTask(task)}>
          <span className="task-icon"><ExperimentOutlined /></span>
          <span><b>{task.name}</b><small>{task.id}</small></span>
        </button>
      ),
    },
    {
      title: '测评对象',
      key: 'target',
      width: 220,
      render: (_: unknown, task: EvaluationTask) => (
        <div className="target-cell">
          <b>{AGENTS.find((agent) => agent.id === task.agentId)?.name}</b>
          <small>{MODELS.find((model) => model.id === task.modelId)?.name}</small>
        </div>
      ),
    },
    { title: '案例', key: 'cases', width: 90, render: (_: unknown, task: EvaluationTask) => `${task.runs.length} 条` },
    { title: '状态', dataIndex: 'status', width: 100, render: (value: EvaluationTask['status']) => <TaskStatusTag status={value} /> },
    { title: '评分', dataIndex: 'scoringStatus', width: 100, render: (value: EvaluationTask['scoringStatus']) => <ScoreStatusTag status={value} /> },
    { title: '发起人', dataIndex: 'creator', width: 100 },
    { title: '发起时间', dataIndex: 'createdAt', width: 150 },
    { title: '', width: 48, render: (_: unknown, task: EvaluationTask) => <Button type="text" icon={<ArrowRightOutlined />} onClick={() => onOpenTask(task)} /> },
  ];

  return (
    <Table
      dataSource={tasks}
      columns={compact ? columns.filter((_, index) => ![4, 5].includes(index)) : columns}
      rowKey="id"
      pagination={false}
      scroll={{ x: compact ? 760 : 1050 }}
    />
  );
}
