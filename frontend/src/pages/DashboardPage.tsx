import { Button, Progress, Space, Typography } from 'antd';
import {
  ArrowRightOutlined,
  BarChartOutlined,
  BookOutlined,
  BranchesOutlined,
  CheckCircleFilled,
  ExperimentOutlined,
  RobotOutlined,
  RocketOutlined,
} from '@ant-design/icons';
import { AGENTS, CASES } from '../mock/data';
import type { EvaluationTask } from '../types';
import { MetricCard, MiniDonut } from '../components/common/MetricCard';
import { TaskTable } from '../components/tasks/TaskTable';

const { Title, Paragraph } = Typography;

interface DashboardPageProps {
  tasks: EvaluationTask[];
  onNavigate: (page: string) => void;
  onOpenTask: (task: EvaluationTask) => void;
}

export function DashboardPage({ tasks, onNavigate, onOpenTask }: DashboardPageProps) {
  const recent = tasks[0];
  const totalRuns = tasks.reduce((sum, task) => sum + task.runs.length, 0);
  const successRuns = tasks.reduce((sum, task) => sum + task.runs.filter((run) => run.status === 'success').length, 0);
  const scoredRuns = recent.runs.filter((run) => run.score);
  const latestScore = Math.round(
    scoredRuns.reduce((sum, run) => {
      const dims = Object.values(run.score?.dims || {});
      return sum + dims.reduce((a, b) => a + b, 0) / Math.max(dims.length, 1);
    }, 0) / Math.max(scoredRuns.length, 1),
  );

  return (
    <div className="page-stack">
      <section className="welcome-panel">
        <div className="welcome-copy">
          <div className="eyebrow"><span /> AI CODING AGENT EVALUATION</div>
          <Title level={2}>早上好，赵启铭</Title>
          <Paragraph>今天可以从一次标准化测评开始，验证 Agent 在真实研发场景中的表现。</Paragraph>
          <Space size={12} wrap>
            <Button type="primary" size="large" icon={<RocketOutlined />} onClick={() => onNavigate('create')}>创建测评任务</Button>
            <Button size="large" icon={<BookOutlined />} onClick={() => onNavigate('cases')}>浏览案例库</Button>
          </Space>
        </div>
        <div className="welcome-visual" aria-hidden="true">
          <div className="orbit orbit-one" />
          <div className="orbit orbit-two" />
          <div className="core-mark"><RobotOutlined /></div>
          <div className="floating-node node-a">Agent</div>
          <div className="floating-node node-b">Cases</div>
          <div className="floating-node node-c">Score</div>
        </div>
      </section>

      <section className="metrics-grid">
        <MetricCard label="累计测评" value={tasks.length + 21} note="本月新增 8 次" tone="green" icon={<ExperimentOutlined />} />
        <MetricCard label="已沉淀案例" value={CASES.length} note="覆盖 5 个研发分类" tone="blue" icon={<BookOutlined />} />
        <MetricCard label="整体成功率" value={`${Math.round((successRuns / totalRuns) * 100)}%`} note="较上周提升 4.2%" tone="orange" icon={<CheckCircleFilled />} />
        <MetricCard label="已接入组合" value={AGENTS.length * 8} note="3 Agents · 8 Models" tone="purple" icon={<BranchesOutlined />} />
      </section>

      <section className="dashboard-grid">
        <div className="surface-card span-8">
          <div className="section-head">
            <div><h3>最近一次测评</h3><p>{recent.name}</p></div>
            <Button type="text" onClick={() => onOpenTask(recent)}>查看详情 <ArrowRightOutlined /></Button>
          </div>
          <div className="task-overview">
            <div className="task-score"><span>综合评分</span><strong>{latestScore}</strong><em>/ 100</em></div>
            <div className="task-donuts">
              <MiniDonut value={80} label="执行成功率" />
              <MiniDonut value={92} label="案例有效率" color="#d9764a" />
            </div>
            <div className="task-summary">
              <div><span>测评对象</span><b>Pi Agent · Ultra-3.5</b></div>
              <div><span>案例范围</span><b>{recent.runs.length} 条 · 4 个分类</b></div>
              <div><span>完成时间</span><b>07-15 11:48</b></div>
            </div>
          </div>
          <div className="dimension-bars">
            {[['正确性', 88], ['功能完整性', 84], ['代码质量', 91], ['安全性', 86]].map(([label, value]) => (
              <div className="dimension-bar" key={label}>
                <span>{label}</span>
                <Progress percent={Number(value)} showInfo={false} strokeColor="#4f7a5b" trailColor="#ebe8e0" />
                <b>{value}</b>
              </div>
            ))}
          </div>
        </div>

        <div className="surface-card span-4">
          <div className="section-head">
            <div><h3>Agent 表现</h3><p>最近 30 天平均得分</p></div>
            <BarChartOutlined className="section-icon" />
          </div>
          <div className="rank-list">
            {[
              ['Pi Agent', 'v2.3.1', 88, '#4f7a5b'],
              ['OpenCode', 'v0.9.4', 82, '#d9764a'],
              ['DevAgent CLI', 'v1.8.0', 76, '#71829c'],
            ].map(([name, version, score, color], index) => (
              <div className="rank-row" key={name}>
                <span className={`rank-index rank-${index + 1}`}>{index + 1}</span>
                <div className="rank-main">
                  <div><b>{name}</b><small>{version}</small></div>
                  <Progress percent={Number(score)} showInfo={false} strokeColor={String(color)} size="small" />
                </div>
                <strong>{score}</strong>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="surface-card">
        <div className="section-head">
          <div><h3>近期测评记录</h3><p>快速回到最近处理过的任务</p></div>
          <Button type="text" onClick={() => onNavigate('records')}>查看全部 <ArrowRightOutlined /></Button>
        </div>
        <TaskTable tasks={tasks.slice(0, 3)} onOpenTask={onOpenTask} compact />
      </section>
    </div>
  );
}
