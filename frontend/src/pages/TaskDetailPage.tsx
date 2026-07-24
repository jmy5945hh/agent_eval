import { useState } from 'react';
import type { Key } from 'react';
import {
  Button,
  Dropdown,
  Modal,
  Popconfirm,
  Progress,
  Select,
  Space,
  Table,
  Tag,
  message,
} from 'antd';
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  BarChartOutlined,
  DeleteOutlined,
  ExportOutlined,
  ExperimentOutlined,
  MoreOutlined,
  PlusOutlined,
  ReloadOutlined,
  StopOutlined,
} from '@ant-design/icons';
import { AGENTS, AGENT_VERSIONS, CASES, MODELS } from '../mock/data';
import type { CaseRun, EvaluationCase, EvaluationTask } from '../types';
import { RunStatusTag, ScoreStatusTag, TaskStatusTag } from '../components/StatusTag';
import { RunDrawer } from '../components/tasks/RunDrawer';
import { ScoringModal } from '../components/tasks/ScoringModal';
import { difficultyClass } from '../components/cases/caseVisuals';

function AddCasesModal({
  open,
  existingCaseIds,
  onCancel,
  onAdd,
}: {
  open: boolean;
  existingCaseIds: string[];
  onCancel: () => void;
  onAdd: (caseIds: string[]) => void;
}) {
  const [selected, setSelected] = useState<Key[]>([]);
  const available = CASES.filter((item) => !existingCaseIds.includes(item.id));

  return (
    <Modal
      open={open}
      title="动态添加测评案例"
      width={820}
      onCancel={onCancel}
      okText={`添加 ${selected.length} 条案例`}
      okButtonProps={{ disabled: selected.length === 0 }}
      onOk={() => {
        onAdd(selected.map(String));
        setSelected([]);
      }}
    >
      <p className="modal-helper">新案例将追加到当前执行队列末尾，并从“队列中”状态开始。</p>
      <Table
        rowSelection={{ selectedRowKeys: selected, onChange: setSelected }}
        dataSource={available}
        rowKey="id"
        pagination={{ pageSize: 6, showSizeChanger: false }}
        columns={[
          { title: '案例', dataIndex: 'name', render: (_: string, record: EvaluationCase) => <div className="simple-case"><b>{record.name}</b><small>{record.code}</small></div> },
          { title: '分类', dataIndex: 'category', render: (value: string) => <Tag className="soft-tag">{value}</Tag> },
          { title: '难度', dataIndex: 'difficulty', width: 90, render: (value: string) => <span className={`level-pill ${difficultyClass[value]}`}><i />{value}</span> },
          { title: '仓库', dataIndex: 'repo', ellipsis: true, render: (value: string) => value.replace('git.example.com/', '') },
        ]}
      />
    </Modal>
  );
}

export function TaskDetailPage({ task, onBack }: { task: EvaluationTask; onBack: () => void }) {
  const [runs, setRuns] = useState<CaseRun[]>(task.runs);
  const [selectedRun, setSelectedRun] = useState<CaseRun | null>(null);
  const [scoreModal, setScoreModal] = useState(false);
  const [addCasesOpen, setAddCasesOpen] = useState(false);
  const isRunning = task.status === 'running';
  const success = runs.filter((run) => run.status === 'success').length;
  const failed = runs.filter((run) => run.status === 'failed').length;
  const completed = success + failed + runs.filter((run) => run.status === 'cancelled').length;
  const progress = Math.round((completed / Math.max(runs.length, 1)) * 100);
  const agentVersion = AGENT_VERSIONS.find((version) => version.id === task.agentVersionId)
    || AGENT_VERSIONS.find((version) => version.agentId === task.agentId && version.latest);

  const terminateCase = (caseId: string) => {
    setRuns((current) => current.map((run) => run.caseId === caseId ? { ...run, status: 'cancelled' } : run));
    message.success('已终止该案例执行');
  };

  const addCases = (caseIds: string[]) => {
    setRuns((current) => [
      ...current,
      ...caseIds.map((caseId): CaseRun => ({
        caseId,
        status: 'queued',
        attempts: 0,
        durationMs: 0,
        tokensIn: 0,
        tokensOut: 0,
      })),
    ]);
    setAddCasesOpen(false);
    message.success(`已向队列添加 ${caseIds.length} 条案例`);
  };

  return (
    <div className="page-stack task-detail-page">
      <button className="back-link" onClick={onBack}><ArrowLeftOutlined /> 返回任务列表</button>
      <section className="task-detail-head surface-card">
        <div className="task-detail-title">
          <div className="task-icon large"><ExperimentOutlined /></div>
          <div>
            <Space><TaskStatusTag status={task.status} /><ScoreStatusTag status={task.scoringStatus} /></Space>
            <h2>{task.name}</h2>
            <p>{task.id} · 由 {task.creator} 发起于 {task.createdAt}</p>
          </div>
        </div>
        <Space wrap>
          <Button icon={<ReloadOutlined />}>复用配置</Button>
          {!isRunning && <Button type="primary" icon={<BarChartOutlined />} onClick={() => setScoreModal(true)}>发起自动评分</Button>}
          <Dropdown menu={{ items: [{ key: 'export', icon: <ExportOutlined />, label: '导出评分明细' }, { key: 'cancel', icon: <DeleteOutlined />, label: '取消任务', danger: true }] }}>
            <Button icon={<MoreOutlined />} />
          </Dropdown>
        </Space>
      </section>

      <section className="task-detail-grid">
        <div className="surface-card progress-panel">
          <div className="section-head"><div><h3>{isRunning ? '执行进度' : '执行结果'}</h3><p>串行队列 · 实时汇总</p></div><strong>{progress}%</strong></div>
          <Progress percent={progress} showInfo={false} strokeColor="#4f7a5b" />
          <div className="progress-stats">
            <div><span>总案例</span><b>{runs.length}</b></div>
            <div><span className="dot success" />成功<b>{success}</b></div>
            <div><span className="dot danger" />失败<b>{failed}</b></div>
            <div><span className="dot warning" />队列中<b>{runs.filter((run) => run.status === 'queued').length}</b></div>
          </div>
        </div>
        <div className="surface-card task-config-panel">
          <div><span>Agent</span><b>{AGENTS.find((agent) => agent.id === task.agentId)?.name}</b><small>{agentVersion?.version || '未记录版本'}</small></div>
          <div><span>测评模型</span><b>{MODELS.find((model) => model.id === task.modelId)?.name}</b><small>{MODELS.find((model) => model.id === task.modelId)?.tier}</small></div>
          <div><span>评分标准</span><b>{task.standardVersion || 'v2.0'}</b><small>通用评分标准</small></div>
        </div>
      </section>

      <section className="surface-card table-card">
        <div className="section-head">
          <div><h3>案例执行队列</h3><p>点击案例查看完整执行轨迹；运行中和队列中的案例可单独终止</p></div>
          <Space>
            <Select defaultValue="all" options={[{ value: 'all', label: '全部状态' }, { value: 'success', label: '成功' }, { value: 'failed', label: '失败' }]} />
            <Button type="primary" icon={<PlusOutlined />} disabled={!isRunning} onClick={() => setAddCasesOpen(true)}>添加案例</Button>
          </Space>
        </div>
        <Table
          dataSource={runs}
          rowKey="caseId"
          pagination={false}
          onRow={(run) => ({ onClick: () => setSelectedRun(run), className: 'clickable-row' })}
          columns={[
            {
              title: '案例',
              dataIndex: 'caseId',
              render: (id: string) => {
                const item = CASES.find((caseItem) => caseItem.id === id);
                return <div className="simple-case"><b>{item?.name || id}</b><small>{item?.code} · {item?.category}</small></div>;
              },
            },
            { title: '状态', dataIndex: 'status', width: 110, render: (value: CaseRun['status']) => <RunStatusTag status={value} /> },
            { title: '轮次', dataIndex: 'rounds', width: 80, render: (value?: number) => value ? `${value} 轮` : '—' },
            { title: 'Token', width: 140, render: (_: unknown, run: CaseRun) => run.tokensIn ? `${(((run.tokensIn || 0) + (run.tokensOut || 0)) / 1000).toFixed(1)}k` : '—' },
            { title: '耗时', dataIndex: 'durationMs', width: 110, render: (value?: number) => value ? `${Math.floor(value / 60000)}m ${Math.floor((value % 60000) / 1000)}s` : '—' },
            { title: '评分', width: 100, render: (_: unknown, run: CaseRun) => run.score ? `${Math.round(Object.values(run.score.dims).reduce((a, b) => a + b, 0) / Object.values(run.score.dims).length)} 分` : '—' },
            {
              title: '操作',
              width: 130,
              render: (_: unknown, run: CaseRun) => (
                <Space onClick={(event) => event.stopPropagation()}>
                  {(run.status === 'running' || run.status === 'queued') ? (
                    <Popconfirm
                      title="终止单条案例"
                      description="终止后将保留当前轨迹，该案例不再继续执行。"
                      okText="确认终止"
                      cancelText="取消"
                      onConfirm={() => terminateCase(run.caseId)}
                    >
                      <Button type="link" danger size="small" icon={<StopOutlined />}>终止</Button>
                    </Popconfirm>
                  ) : (
                    <Button type="link" size="small" onClick={() => setSelectedRun(run)}>详情</Button>
                  )}
                  <Button type="text" size="small" icon={<ArrowRightOutlined />} />
                </Space>
              ),
            },
          ]}
        />
      </section>
      <RunDrawer run={selectedRun} onClose={() => setSelectedRun(null)} />
      <ScoringModal open={scoreModal} onClose={() => setScoreModal(false)} />
      <AddCasesModal open={addCasesOpen} existingCaseIds={runs.map((run) => run.caseId)} onCancel={() => setAddCasesOpen(false)} onAdd={addCases} />
    </div>
  );
}
