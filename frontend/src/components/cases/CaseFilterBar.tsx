import type { ReactNode } from 'react';
import { Button, Input, Select } from 'antd';
import { FilterOutlined, SearchOutlined } from '@ant-design/icons';
import { CATEGORIES } from '../../mock/data';

interface CaseFilterBarProps {
  search: string;
  setSearch: (value: string) => void;
  category: string;
  setCategory: (value: string) => void;
  count: number;
  extra?: ReactNode;
}

export function CaseFilterBar({
  search,
  setSearch,
  category,
  setCategory,
  count,
  extra,
}: CaseFilterBarProps) {
  return (
    <div className="filter-bar">
      <Input
        prefix={<SearchOutlined />}
        placeholder="搜索案例名称、描述或仓库"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        allowClear
        className="search-input"
      />
      <Select
        value={category}
        onChange={setCategory}
        options={[{ value: 'all', label: '全部分类' }, ...CATEGORIES.map((item) => ({ value: item, label: item }))]}
        className="filter-select"
      />
      <Button icon={<FilterOutlined />}>更多筛选</Button>
      <span className="filter-count">共 {count} 条案例</span>
      {extra}
    </div>
  );
}
