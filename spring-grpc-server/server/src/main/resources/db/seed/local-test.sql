-- local 프로파일(jdbc:h2:mem:local-test) 전용 샘플 데이터.
-- spring.sql.init.data-locations 로 명시한 프로파일에서만 실행된다.
INSERT INTO tb_list_view (id, name, status, category, frequency) VALUES
  ('rpt-001', 'Daily Traffic Report',      'CREATED',  'BASIC',   'EVERY_DAY'),
  ('rpt-002', 'Weekly Threat Summary',     'CREATED',  'UNIFIED', 'EVERY_WEEK'),
  ('rpt-003', 'Monthly Compliance Audit',  'PENDING',  'UNIFIED', 'EVERY_MONTH_DAY'),
  ('rpt-004', 'Ad-hoc Traffic Query',      'CREATING', 'QUERY',   'IMMEDIATE'),
  ('rpt-005', 'Yearly Capacity Plan',      'CREATED',  'BASIC',   'EVERY_YEAR'),
  ('rpt-006', 'Nightly Backup Check',      'FAILED',   'BASIC',   'SPECIFIC_TIME'),
  ('rpt-007', 'Legacy Traffic Archive',    'DELETED',  'QUERY',   'EVERY_DAY'),
  ('rpt-008', 'Quarterly Threat Review',   'PENDING',  'UNIFIED', 'EVERY_MONTH_DAY');
