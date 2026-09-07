-- local 프로파일(jdbc:h2:mem:local-test) 전용 샘플 데이터.
-- spring.sql.init.data-locations 로 명시한 프로파일에서만 실행된다.
INSERT INTO tb_list_view (id, name, status, category, frequency, created_at, modified_at) VALUES
  ('rpt-001', 'Daily Traffic Report',      'CREATED',  'BASIC',   'EVERY_DAY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-002', 'Weekly Threat Summary',     'CREATED',  'UNIFIED', 'EVERY_WEEK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-003', 'Monthly Compliance Audit',  'PENDING',  'UNIFIED', 'EVERY_MONTH_DAY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-004', 'Ad-hoc Traffic Query',      'CREATING', 'QUERY',   'IMMEDIATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-005', 'Yearly Capacity Plan',      'CREATED',  'BASIC',   'EVERY_YEAR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-006', 'Nightly Backup Check',      'FAILED',   'BASIC',   'SPECIFIC_TIME', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-007', 'Legacy Traffic Archive',    'DELETED',  'QUERY',   'EVERY_DAY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-008', 'Quarterly Threat Review',   'PENDING',  'UNIFIED', 'EVERY_MONTH_DAY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-009', 'Ad-hoc Security Scan',      'CREATED',  'QUERY',   'IMMEDIATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('rpt-010', 'Annual Compliance Report',  'CREATED',  'BASIC',   'EVERY_YEAR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
