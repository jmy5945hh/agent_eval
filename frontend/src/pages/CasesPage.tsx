import { useState } from 'react';
import { Button, Dropdown, Table, Tag } from 'antd';
import {
  AppstoreOutlined,
  BranchesOutlined,
  DeleteOutlined,
  EditOutlined,
  MoreOutlined,
  PlusOutlined,
} from '@ant-design/icons';
import { CASES, CATEGORIES } from '../mock/data';
import type { EvaluationCase } from '../types';
import { CaseFilterBar } from '../components/cases/CaseFilterBar';
import { CaseFormModal } from '../components/cases/CaseFormModal';
import { categoryIcons, difficultyClass } from '../components/cases/caseVisuals';

export function CasesPage({ onSelectCase }: { onSelectCase: (item: EvaluationCase) => void }) {
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('all');
  const [addOpen, setAddOpen] = useState(false);
  const filtered = CASES.filter((item) => {
    const matchesCategory = category === 'all' || item.category === category;
    const text = `${item.name}${item.prompt}${item.repo}`.toLowerCase();
    return matchesCategory && text.includes(search.toLowerCase());
  });

  const columns = [
    {
      title: '案例',
      dataIndex: 'name',
      width: 290,
      render: (_: string, record: EvaluationCase) => (
        <button className="case-link" onClick={() => onSelectCase(record)}>
          <span className="case-type-icon">{categoryIcons[record.category]}</span>
          <span><b>{record.name}</b><small>{record.code} · v{record.version}</small></span>
        </button>
      ),
    },
    { title: '分类', dataIndex: 'category', width: 120, render: (value: string) => <Tag className="soft-tag">{value}</Tag> },
    { title: '难度', dataIndex: 'difficulty', width: 100, render: (value: string) => <span className={`level-pill ${difficultyClass[value]}`}><i />{value}</span> },
    {
      title: '目标仓库 / 分支',
      dataIndex: 'repo',
      ellipsis: true,
      render: (_: string, record: EvaluationCase) => (
        <div className="repo-cell">
          <span>{record.repo.replace('git.example.com/', '')}</span>
          <small><BranchesOutlined /> {record.branch}</small>
        </div>
      ),
    },
    { title: '重要性', dataIndex: 'importance', width: 100 },
    { title: '更新时间', dataIndex: 'createdAt', width: 150, render: (value: string) => value.slice(0, 10) },
    {
      title: '',
      width: 56,
      render: () => (
        <Dropdown menu={{ items: [{ key: 'edit', icon: <EditOutlined />, label: '编辑' }, { key: 'delete', icon: <DeleteOutlined />, label: '删除', danger: true }] }}>
          <Button type="text" icon={<MoreOutlined />} />
        </Dropdown>
      ),
    },
  ];

  return (
    <div className="page-stack">
      <div className="category-strip">
        <button className={category === 'all' ? 'active' : ''} onClick={() => setCategory('all')}>
          <span className="category-icon"><AppstoreOutlined /></span><b>全部案例</b><small>{CASES.length} 条</small>
        </button>
        {CATEGORIES.map((item) => (
          <button className={category === item ? 'active' : ''} onClick={() => setCategory(item)} key={item}>
            <span className="category-icon">{categoryIcons[item]}</span>
            <b>{item}</b>
            <small>{CASES.filter((caseItem) => caseItem.category === item).length} 条</small>
          </button>
        ))}
      </div>
      <section className="surface-card table-card">
        <div className="section-head section-head-actions">
          <div><h3>案例列表</h3><p>场景化用例、Prompt 与标准答案统一管理</p></div>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setAddOpen(true)}>新增案例</Button>
        </div>
        <CaseFilterBar search={search} setSearch={setSearch} category={category} setCategory={setCategory} count={filtered.length} />
        <Table dataSource={filtered} columns={columns} rowKey="id" pagination={{ pageSize: 8, showSizeChanger: false }} scroll={{ x: 960 }} />
      </section>
      <CaseFormModal open={addOpen} onCancel={() => setAddOpen(false)} />
    </div>
  );
}
