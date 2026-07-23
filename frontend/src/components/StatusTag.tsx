import { Tag } from 'antd';
import type { RunStatus, ScoreStatus, TaskStatus } from '../types';
import {
  ClockCircleOutlined, LoadingOutlined, CheckCircleFilled,
  CloseCircleFilled, MinusCircleOutlined, EyeInvisibleOutlined,
} from '@ant-design/icons';

const RUN_STATUS = {
  queued: { color: 'gold', icon: <ClockCircleOutlined />, text: '队列中' },
  running: { color: 'processing', icon: <LoadingOutlined />, text: '运行中' },
  success: { color: 'success', icon: <CheckCircleFilled />, text: '成功' },
  failed: { color: 'error', icon: <CloseCircleFilled />, text: '失败' },
  cancelled: { color: 'default', icon: <MinusCircleOutlined />, text: '已取消' },
};

export function RunStatusTag({ status, removed }: { status: RunStatus; removed?: boolean }) {
  if (removed) {
    return <Tag icon={<EyeInvisibleOutlined />} color="default">已移除</Tag>;
  }
  const s = RUN_STATUS[status] || RUN_STATUS.queued;
  return <Tag icon={s.icon} color={s.color}>{s.text}</Tag>;
}

const TASK_STATUS = {
  running: { color: 'processing', text: '执行中' },
  completed: { color: 'success', text: '已完成' },
  cancelled: { color: 'default', text: '已取消' },
};

export function TaskStatusTag({ status }: { status: TaskStatus }) {
  const s = TASK_STATUS[status] || TASK_STATUS.running;
  return <Tag color={s.color}>{s.text}</Tag>;
}

const SCORE_STATUS = {
  idle: { color: 'default', text: '未评分' },
  scoring: { color: 'processing', text: '评分中' },
  scored: { color: 'success', text: '已评分' },
  confirmed: { color: 'green', text: '已确认' },
};

export function ScoreStatusTag({ status }: { status: ScoreStatus }) {
  const s = SCORE_STATUS[status] || SCORE_STATUS.idle;
  return <Tag color={s.color}>{s.text}</Tag>;
}
