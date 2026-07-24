import { useMemo, useState } from 'react';
import type { Key } from 'react';
import {
  Button,
  Input,
  Modal,
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
  FileSearchOutlined,
  RobotOutlined,
  RocketOutlined,
} from '@ant-design/icons';
import { AGENTS, AGENT_VERSIONS, CASES, MODELS, SCORING_STANDARDS, currentStandard } from '../mock/data';
import type { AgentVersion, CreateTaskPayload, EvaluationCase, ScoringStandard } from '../types';
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
        <div><h4>选择版本</h4></div>
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
            <span className="version-name">{version.version}{version.latest && <Tag color="success">最新</Tag>}</span>
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
    AGENT_VERSIONS.find((version) => version.agentId === AGENTS[0].id && version.latest)?.id || '',
  );
  const availableModels = useMemo(() => MODELS.filter((model) => model.enabled && !model.scoring).slice(0, 10), []);
  const [modelId, setModelId] = useState(availableModels[0].id);
  const [selectedCases, setSelectedCases] = useState<Key[]>(['C001', 'C004', 'C008', 'C010']);
  const [scoringStandardId, setScoringStandardId] = useState(currentStandard(SCORING_STANDARDS).id);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('all');
  const [taskName, setTaskName] = useState('');
  const [caseDetail, setCaseDetail] = useState<EvaluationCase | null>(null);
  const [standardDetail, setStandardDetail] = useState<ScoringStandard | null>(null);

  const versions = AGENT_VERSIONS.filter((version) => version.agentId === agentId && version.enabled);
  const selectedAgent = AGENTS.find((agent) => agent.id === agentId);
  const selectedVersion = AGENT_VERSIONS.find((version) => version.id === agentVersionId);
  const selectedModel = availableModels.find((model) => model.id === modelId);
  const selectedStandard = SCORING_STANDARDS.find((standard) => standard.id === scoringStandardId);
  const filtered = CASES.filter(
    (item) =>
      (category === 'all' || item.category === category) &&
      `${item.name}${item.prompt}${item.repo}`.toLowerCase().includes(search.toLowerCase()),
  );
  const selectedCaseItems = CASES.filter((item) => selectedCases.includes(item.id));
  const categorySummary = Object.entries(
    selectedCaseItems.reduce<Record<string, number>>((summary, item) => {
      summary[item.category] = (summary[item.category] || 0) + 1;
      return summary;
    }, {}),
  );
  const difficultySummary = Object.entries(
    selectedCaseItems.reduce<Record<string, number>>((summary, item) => {
      summary[item.difficulty] = (summary[item.difficulty] || 0) + 1;
      return summary;
    }, {}),
  );

  const selectAgent = (nextAgentId: string) => {
    setAgentId(nextAgentId);
    const nextVersions = AGENT_VERSIONS.filter((version) => version.agentId === nextAgentId && version.enabled);
    setAgentVersionId(nextVersions.find((version) => version.latest)?.id || nextVersions[0]?.id || '');
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
      <div className="wizard-intro"><span>02</span><div><h3>选择测评模型</h3></div></div>
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
            icon={<DatabaseOutlined />}
            compact
          />
        ))}
      </div>
    </div>,
    <div className="wizard-section" key="cases">
      <div className="wizard-intro"><span>03</span><div><h3>选择测评案例</h3></div></div>
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
        className="case-selection-table"
        rowClassName="selectable-case-row"
        onRow={(record) => ({
          onClick: () => {
            setSelectedCases((current) =>
              current.includes(record.id)
                ? current.filter((key) => key !== record.id)
                : [...current, record.id],
            );
          },
        })}
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
    <div className="wizard-section" key="standard">
      <div className="wizard-intro"><span>04</span><div><h3>选择评分标准模板</h3><p>评分模板将作为本次测评的关键元信息，任务创建后随任务一并保存。</p></div></div>
      <div className="scoring-template-grid">
        {SCORING_STANDARDS.map((standard) => (
          <div
            role="button"
            tabIndex={0}
            className={`scoring-template-card ${scoringStandardId === standard.id ? 'active' : ''}`}
            key={standard.id}
            onClick={() => setScoringStandardId(standard.id)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                setScoringStandardId(standard.id);
              }
            }}
          >
            <div className="scoring-template-head">
              <span><FileSearchOutlined /></span>
              <div><b>通用评分标准 {standard.version}</b><small>更新于 {standard.updatedAt}</small></div>
              {standard.current && <Tag color="success">当前版本</Tag>}
            </div>
            <p>{standard.note}</p>
            <div className="scoring-template-dims">
              {standard.dimensions.map((dimension) => (
                <span key={dimension.key}>{dimension.label}<b>{dimension.weight}%</b></span>
              ))}
            </div>
            <Button
              type="link"
              className="scoring-template-detail"
              onClick={(event) => {
                event.stopPropagation();
                setStandardDetail(standard);
              }}
            >
              查看评分详情细则
            </Button>
          </div>
        ))}
      </div>
    </div>,
    <div className="wizard-section" key="confirm">
      <div className="wizard-intro"><span>05</span><div><h3>确认测评配置</h3><p>核对产品版本、模型、案例范围和评分标准，无误后进入串行执行队列。</p></div></div>
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
            <div className="confirm-block-head"><h4>案例汇总</h4><span>共 {selectedCases.length} 条</span></div>
            <div className="case-summary-grid">
              <div className="case-summary-total">
                <small>案例总数</small>
                <strong>{selectedCases.length}</strong>
                <span>条</span>
              </div>
              <div className="case-summary-group">
                <small>类别分布</small>
                <div>
                  {categorySummary.map(([name, count]) => (
                    <Tag key={name}>{name} <b>{count}</b></Tag>
                  ))}
                </div>
              </div>
              <div className="case-summary-group">
                <small>难度分布</small>
                <div>
                  {difficultySummary.map(([name, count]) => (
                    <Tag key={name} className={`summary-level ${difficultyClass[name]}`}>{name}难度 <b>{count}</b></Tag>
                  ))}
                </div>
              </div>
            </div>
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
          <div className="summary-picked">
            <span><FileSearchOutlined /></span>
            <div><small>评分标准模板</small><b>通用评分标准</b><em>{selectedStandard?.version}</em></div>
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
            { title: '评分标准', description: step > 2 ? selectedStandard?.version : '待选择' },
            { title: '确认发起', description: '检查配置' },
          ]}
        />
      </div>
      <section className="surface-card wizard-card">{stepContent[step]}</section>
      <div className="wizard-actions">
        <Button size="large" disabled={step === 0} icon={<ArrowLeftOutlined />} onClick={() => setStep(step - 1)}>上一步</Button>
        {step < 4 ? (
          <Button type="primary" size="large" onClick={() => setStep(step + 1)} disabled={(step === 0 && !agentVersionId) || (step === 2 && selectedCases.length === 0) || (step === 3 && !scoringStandardId)}>
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
              scoringStandardId,
              taskName,
            })}
          >
            发起测评
          </Button>
        )}
      </div>
      <CaseDetailModal item={caseDetail} onClose={() => setCaseDetail(null)} />
      <Modal
        open={Boolean(standardDetail)}
        title={standardDetail ? `评分详情细则 · 通用评分标准 ${standardDetail.version}` : ''}
        width={720}
        onCancel={() => setStandardDetail(null)}
        footer={<Button type="primary" onClick={() => setStandardDetail(null)}>关闭</Button>}
      >
        {standardDetail && (
          <div className="standard-detail">
            <p>{standardDetail.note}</p>
            <div className="standard-detail-head">
              <span>评分维度</span><span>权重</span><span>评分关注点</span>
            </div>
            {standardDetail.dimensions.map((dimension) => (
              <div className="standard-detail-row" key={dimension.key}>
                <b>{dimension.label}</b>
                <Tag color="success">{dimension.weight}%</Tag>
                <span>{dimension.desc}</span>
              </div>
            ))}
            <div className="standard-detail-total">
              <span>权重合计</span>
              <b>{standardDetail.dimensions.reduce((sum, dimension) => sum + dimension.weight, 0)}%</b>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
