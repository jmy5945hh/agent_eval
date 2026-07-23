import { useMemo, useState } from 'react';
import type { Key } from 'react';
import {
  Button,
  Input,
  Steps,
  Table,
  Tag,
} from 'antd';
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  CheckCircleFilled,
  ClockCircleOutlined,
  DatabaseOutlined,
  EyeOutlined,
  RobotOutlined,
  RocketOutlined,
} from '@ant-design/icons';
import { AGENTS, AGENT_VERSIONS, CASES, MODELS } from '../mock/data';
import type { AgentVersion, CreateTaskPayload, EvaluationCase } from '../types';
import { SelectionCard } from '../components/common/SelectionCard';
import { CaseFilterBar } from '../components/cases/CaseFilterBar';
import { CaseDetailModal } from '../components/cases/CaseDetail';
import { difficultyClass } from '../components/cases/caseVisuals';

function VersionList({
  versions,
  selected,
  onSelect,
}: {
  versions: AgentVersion[];
  selected: string;
  onSelect: (id: string) => void;
}) {
  return (
    <div className="agent-version-panel">
      <div className="version-panel-head">
        <div><h4>选择产品版本</h4><p>同一 Agent 的不同版本可分别创建任务进行横向对比</p></div>
        <Tag>{versions.length} 个可用版本</Tag>
      </div>
      <div className="version-list">
        {versions.map((version) => (
          <button
            key={version.id}
            className={`version-row ${selected === version.id ? 'active' : ''}`}
            onClick={() => onSelect(version.id)}
          >
            <span className="version-radio">{selected === version.id && <i />}</span>
            <span className="version-name">{version.version}{version.recommended && <Tag color="success">推荐</Tag>}</span>
            <span className="version-note">{version.notes}</span>
            <span className="version-date">发布于 {version.releasedAt}</span>
          </button>
        ))}
      </div>
    </div>
  );
}

export function CreateTaskPage({ onLaunch }: { onLaunch: (payload: CreateTaskPayload) => void }) {
  const [step, setStep] = useState(0);
  const [agentId, setAgentId] = useState(AGENTS[0].id);
  const [agentVersionId, setAgentVersionId] = useState(
    AGENT_VERSIONS.find((version) => version.agentId === AGENTS[0].id && version.recommended)?.id || '',
  );
  const availableModels = useMemo(() => MODELS.filter((model) => model.enabled && !model.scoring).slice(0, 10), []);
  const [modelId, setModelId] = useState(availableModels[0].id);
  const [selectedCases, setSelectedCases] = useState<Key[]>(['C001', 'C004', 'C008', 'C010']);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('all');
  const [taskName, setTaskName] = useState('');
  const [caseDetail, setCaseDetail] = useState<EvaluationCase | null>(null);

  const versions = AGENT_VERSIONS.filter((version) => version.agentId === agentId && version.enabled);
  const selectedAgent = AGENTS.find((agent) => agent.id === agentId);
  const selectedVersion = AGENT_VERSIONS.find((version) => version.id === agentVersionId);
  const selectedModel = availableModels.find((model) => model.id === modelId);
  const filtered = CASES.filter(
    (item) =>
      (category === 'all' || item.category === category) &&
      `${item.name}${item.prompt}${item.repo}`.toLowerCase().includes(search.toLowerCase()),
  );

  const selectAgent = (nextAgentId: string) => {
    setAgentId(nextAgentId);
    const nextVersions = AGENT_VERSIONS.filter((version) => version.agentId === nextAgentId && version.enabled);
    setAgentVersionId(nextVersions.find((version) => version.recommended)?.id || nextVersions[0]?.id || '');
  };

  const stepContent = [
    <div className="wizard-section" key="agent">
      <div className="wizard-intro"><span>01</span><div><h3>选择参测 Agent 与版本</h3><p>先选择 Agent 产品，再指定参与测评的产品版本。</p></div></div>
      <div className="selection-grid agent-product-grid">
        {AGENTS.map((agent) => (
          <SelectionCard
            key={agent.id}
            active={agentId === agent.id}
            onClick={() => selectAgent(agent.id)}
            title={agent.name}
            subtitle={agent.vendor}
            description={agent.desc}
            meta={`${AGENT_VERSIONS.filter((version) => version.agentId === agent.id).length} 个可用版本`}
            icon={<RobotOutlined />}
            compact
          />
        ))}
      </div>
      <VersionList versions={versions} selected={agentVersionId} onSelect={setAgentVersionId} />
    </div>,
    <div className="wizard-section" key="model">
      <div className="wizard-intro"><span>02</span><div><h3>选择测评模型</h3><p>模型数量由后端动态返回，页面最多展示 10 个已启用模型。</p></div></div>
      <div className="model-count-row">
        <span>可选模型 <b>{availableModels.length}</b> / 10</span>
        <div><span>Fast</span><span>Pro</span><span>Ultra</span></div>
      </div>
      <div className="selection-grid model-grid">
        {availableModels.map((model) => (
          <SelectionCard
            key={model.id}
            active={modelId === model.id}
            onClick={() => setModelId(model.id)}
            title={model.name}
            subtitle={model.tier}
            description={model.desc}
            meta={`${model.provider} · ${model.version}`}
            icon={<DatabaseOutlined />}
            compact
          />
        ))}
      </div>
    </div>,
    <div className="wizard-section" key="cases">
      <div className="wizard-intro"><span>03</span><div><h3>选择测评案例</h3><p>可跨分类组合案例，点击“查看”可在选择前核对完整 Prompt 与标准答案。</p></div></div>
      <CaseFilterBar
        search={search}
        setSearch={setSearch}
        category={category}
        setCategory={setCategory}
        count={filtered.length}
        extra={<div className="selected-count"><CheckCircleFilled /> 已选 {selectedCases.length}</div>}
      />
      <Table
        rowSelection={{ selectedRowKeys: selectedCases, onChange: setSelectedCases }}
        dataSource={filtered}
        rowKey="id"
        pagination={{ pageSize: 6, showSizeChanger: false }}
        columns={[
          { title: '案例', dataIndex: 'name', render: (_: string, record: EvaluationCase) => <div className="simple-case"><b>{record.name}</b><small>{record.code}</small></div> },
          { title: '分类', dataIndex: 'category', render: (value: string) => <Tag className="soft-tag">{value}</Tag> },
          { title: '难度', dataIndex: 'difficulty', width: 90, render: (value: string) => <span className={`level-pill ${difficultyClass[value]}`}><i />{value}</span> },
          { title: '仓库', dataIndex: 'repo', ellipsis: true, render: (value: string) => value.replace('git.example.com/', '') },
          {
            title: '操作',
            width: 90,
            render: (_: unknown, record: EvaluationCase) => (
              <Button
                type="link"
                size="small"
                icon={<EyeOutlined />}
                onClick={(event) => {
                  event.stopPropagation();
                  setCaseDetail(record);
                }}
              >
                查看
              </Button>
            ),
          },
        ]}
      />
    </div>,
    <div className="wizard-section" key="confirm">
      <div className="wizard-intro"><span>04</span><div><h3>确认测评配置</h3><p>核对产品版本、模型和案例范围，无误后进入串行执行队列。</p></div></div>
      <div className="confirm-grid">
        <div className="confirm-main">
          <label>任务名称 <small>选填</small></label>
          <Input
            size="large"
            value={taskName}
            onChange={(event) => setTaskName(event.target.value)}
            placeholder={`${selectedAgent?.name}_${selectedVersion?.version}_${selectedModel?.name}_日期时间_赵启铭`}
          />
          <div className="confirm-block">
            <div className="confirm-block-head"><h4>案例清单</h4><span>{selectedCases.length} 条</span></div>
            {CASES.filter((item) => selectedCases.includes(item.id)).map((item, index) => (
              <div className="confirm-case" key={item.id}>
                <span>{String(index + 1).padStart(2, '0')}</span>
                <div><b>{item.name}</b><small>{item.code} · {item.category} · {item.difficulty}难度</small></div>
              </div>
            ))}
          </div>
        </div>
        <aside className="confirm-aside">
          <h4>配置摘要</h4>
          <div className="summary-picked">
            <span><RobotOutlined /></span>
            <div><small>Agent 产品 / 版本</small><b>{selectedAgent?.name}</b><em>{selectedVersion?.version}</em></div>
          </div>
          <div className="summary-picked">
            <span><DatabaseOutlined /></span>
            <div><small>测评模型</small><b>{selectedModel?.name}</b><em>{selectedModel?.version}</em></div>
          </div>
          <div className="queue-tip"><ClockCircleOutlined /><p><b>串行队列执行</b><br />预计耗时约 {selectedCases.length * 6}–{selectedCases.length * 10} 分钟</p></div>
        </aside>
      </div>
    </div>,
  ];

  return (
    <div className="page-stack">
      <div className="wizard-steps surface-card">
        <Steps
          current={step}
          items={[
            { title: '选择 Agent', description: `${selectedAgent?.name} ${selectedVersion?.version || ''}` },
            { title: '选择模型', description: step > 0 ? selectedModel?.name : '待选择' },
            { title: '选择案例', description: step > 1 ? `${selectedCases.length} 条` : '待选择' },
            { title: '确认发起', description: '检查配置' },
          ]}
        />
      </div>
      <section className="surface-card wizard-card">{stepContent[step]}</section>
      <div className="wizard-actions">
        <Button size="large" disabled={step === 0} icon={<ArrowLeftOutlined />} onClick={() => setStep(step - 1)}>上一步</Button>
        {step < 3 ? (
          <Button type="primary" size="large" onClick={() => setStep(step + 1)} disabled={(step === 0 && !agentVersionId) || (step === 2 && selectedCases.length === 0)}>
            下一步 <ArrowRightOutlined />
          </Button>
        ) : (
          <Button
            type="primary"
            size="large"
            icon={<RocketOutlined />}
            onClick={() => onLaunch({
              agentId,
              agentVersionId,
              modelId,
              selectedCases: selectedCases.map(String),
              taskName,
            })}
          >
            发起测评
          </Button>
        )}
      </div>
      <CaseDetailModal item={caseDetail} onClose={() => setCaseDetail(null)} />
    </div>
  );
}
