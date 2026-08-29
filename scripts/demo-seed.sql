-- =====================================================================
-- DEMO SEED SCRIPT — for screenshots only, NOT a Flyway migration.
-- Run manually against your local dev database after Flyway has
-- applied V1..V8, on a clean/test database (no real data to keep).
--
--   psql -U <user> -d <db> -f demo-seed.sql
--
-- Assumes:
--   - V7 seed admin (admin@salestrack.com) already exists.
--   - V8 deal_stage_history table exists. If you haven't added it yet,
--     delete the "deal_stage_history" section below before running.
-- =====================================================================

-- ── Users (Manager + Sales Reps) ────────────────────────────────────
-- Shared demo password for all three: Demo1234!
INSERT INTO users (name, email, password, role, created_at, updated_at) VALUES
('Ayşe Yıldız',   'ayse@salestrack.com',   '$2b$10$3nkoCFDJvbOVSiuzFA574.B.KD6ICfUKIXaroNIuz3wLqfQNXGWI6', 'MANAGER',   NOW(), NOW()),
('Mehmet Demir',  'mehmet@salestrack.com', '$2b$10$3nkoCFDJvbOVSiuzFA574.B.KD6ICfUKIXaroNIuz3wLqfQNXGWI6', 'SALES_REP', NOW(), NOW()),
('Zeynep Kaya',   'zeynep@salestrack.com', '$2b$10$3nkoCFDJvbOVSiuzFA574.B.KD6ICfUKIXaroNIuz3wLqfQNXGWI6', 'SALES_REP', NOW(), NOW());

-- ── Companies ────────────────────────────────────────────────────────
INSERT INTO companies (name, sector, website, created_at, updated_at) VALUES
('Anka Yazılım A.Ş.',   'Yazılım',   'https://ankayazilim.com',  NOW(), NOW()),
('Marmara Lojistik',    'Lojistik',  'https://marmaralojistik.com', NOW(), NOW()),
('Ege Tekstil San.',    'Tekstil',   'https://egetekstil.com.tr', NOW(), NOW()),
('Boğaziçi Finans',     'Finans',    'https://bogazicifinans.com', NOW(), NOW()),
('Kuzey Enerji',        'Enerji',    NULL, NOW(), NOW());

-- ── Contacts ─────────────────────────────────────────────────────────
INSERT INTO contacts (name, email, phone, position, company_id, created_at, updated_at)
SELECT v.name, v.email, v.phone, v.position, c.id, NOW(), NOW()
FROM (VALUES
  ('Ahmet Kılıç',    'ahmet.kilic@ankayazilim.com',   '0532 111 2233', 'Satın Alma Müdürü',   'Anka Yazılım A.Ş.'),
  ('Elif Şahin',     'elif.sahin@ankayazilim.com',    '0532 111 2244', 'IT Direktörü',        'Anka Yazılım A.Ş.'),
  ('Burak Aydın',    'burak.aydin@marmaralojistik.com','0533 222 3344', 'Operasyon Müdürü',    'Marmara Lojistik'),
  ('Selin Arslan',   'selin.arslan@egetekstil.com.tr','0534 333 4455', 'Genel Müdür',         'Ege Tekstil San.'),
  ('Caner Yavuz',    'caner.yavuz@bogazicifinans.com','0535 444 5566', 'CFO',                 'Boğaziçi Finans'),
  ('Deniz Korkmaz',  'deniz.korkmaz@kuzeyenerji.com', '0536 555 6677', 'Proje Yöneticisi',    'Kuzey Enerji')
) AS v(name, email, phone, position, company_name)
JOIN companies c ON c.name = v.company_name;

-- ── Deals ────────────────────────────────────────────────────────────
INSERT INTO deals (title, amount, stage, expected_close_date, company_id, assigned_user_id, created_at, updated_at)
SELECT v.title, v.amount, v.stage, v.expected_close_date, c.id, u.id, NOW(), NOW()
FROM (VALUES
  ('Anka Yazılım - Kurumsal Lisans Anlaşması', 480000.00, 'NEGOTIATION', CURRENT_DATE + INTERVAL '15 day', 'Anka Yazılım A.Ş.',  'mehmet@salestrack.com'),
  ('Marmara Lojistik - Filo Takip Entegrasyonu', 210000.00, 'PROPOSAL',    CURRENT_DATE + INTERVAL '25 day', 'Marmara Lojistik',   'zeynep@salestrack.com'),
  ('Ege Tekstil - ERP Modernizasyonu',           650000.00, 'QUALIFIED',   CURRENT_DATE + INTERVAL '40 day', 'Ege Tekstil San.',   'mehmet@salestrack.com'),
  ('Boğaziçi Finans - Raporlama Platformu',      320000.00, 'WON',         CURRENT_DATE - INTERVAL '5 day',  'Boğaziçi Finans',    'zeynep@salestrack.com'),
  ('Kuzey Enerji - Saha Operasyon Yazılımı',     175000.00, 'NEW',         CURRENT_DATE + INTERVAL '50 day', 'Kuzey Enerji',       'mehmet@salestrack.com'),
  ('Anka Yazılım - Destek Paketi Yenileme',       95000.00, 'LOST',        CURRENT_DATE - INTERVAL '10 day', 'Anka Yazılım A.Ş.',  'zeynep@salestrack.com')
) AS v(title, amount, stage, expected_close_date, company_name, user_email)
JOIN companies c ON c.name = v.company_name
JOIN users u ON u.email = v.user_email;

-- ── Deal stage history ───────────────────────────────────────────────
-- Skip this block if you have not yet added the V8 deal_stage_history migration.
INSERT INTO deal_stage_history (deal_id, from_stage, to_stage, changed_by_id, created_at, updated_at)
SELECT d.id, h.from_stage, h.to_stage, u.id, NOW() - h.days_ago * INTERVAL '1 day', NOW() - h.days_ago * INTERVAL '1 day'
FROM (VALUES
  ('Anka Yazılım - Kurumsal Lisans Anlaşması', NULL,          'NEW',         'mehmet@salestrack.com', 20),
  ('Anka Yazılım - Kurumsal Lisans Anlaşması', 'NEW',         'QUALIFIED',   'mehmet@salestrack.com', 16),
  ('Anka Yazılım - Kurumsal Lisans Anlaşması', 'QUALIFIED',   'PROPOSAL',    'mehmet@salestrack.com', 10),
  ('Anka Yazılım - Kurumsal Lisans Anlaşması', 'PROPOSAL',    'NEGOTIATION', 'mehmet@salestrack.com', 3),
  ('Boğaziçi Finans - Raporlama Platformu',    NULL,          'NEW',         'zeynep@salestrack.com', 30),
  ('Boğaziçi Finans - Raporlama Platformu',    'NEW',         'QUALIFIED',   'zeynep@salestrack.com', 24),
  ('Boğaziçi Finans - Raporlama Platformu',    'QUALIFIED',   'PROPOSAL',    'zeynep@salestrack.com', 18),
  ('Boğaziçi Finans - Raporlama Platformu',    'PROPOSAL',    'NEGOTIATION', 'zeynep@salestrack.com', 10),
  ('Boğaziçi Finans - Raporlama Platformu',    'NEGOTIATION', 'WON',         'zeynep@salestrack.com', 5)
) AS h(deal_title, from_stage, to_stage, user_email, days_ago)
JOIN deals d ON d.title = h.deal_title
JOIN users u ON u.email = h.user_email;

-- ── Leads ────────────────────────────────────────────────────────────
INSERT INTO leads (name, email, phone, company_name, source, status, assigned_user_id, converted_deal_id, created_at, updated_at)
SELECT v.name, v.email, v.phone, v.company_name, v.source, v.status, u.id, d.id, NOW(), NOW()
FROM (VALUES
  ('Onur Bayrak',   'onur.bayrak@gmail.com',   '0537 666 7788', 'Bayrak İnşaat',      'WEBSITE',    'NEW',          'mehmet@salestrack.com', NULL),
  ('Gizem Polat',   'gizem.polat@gmail.com',   '0538 777 8899', 'Polat Gıda',         'REFERRAL',   'CONTACTED',    'zeynep@salestrack.com', NULL),
  ('Kerem Uslu',    'kerem.uslu@gmail.com',    '0539 888 9900', 'Uslu Otomotiv',      'COLD_CALL',  'QUALIFIED',    'mehmet@salestrack.com', NULL),
  ('Nazlı Ergün',   'nazli.ergun@gmail.com',   '0530 999 0011', 'Ergün Danışmanlık',  'WEBSITE',    'DISQUALIFIED', 'zeynep@salestrack.com', NULL),
  ('Caner Yavuz',   'caner.yavuz@bogazicifinans.com', '0535 444 5566', 'Boğaziçi Finans', 'REFERRAL', 'CONVERTED',  'zeynep@salestrack.com', 'Boğaziçi Finans - Raporlama Platformu')
) AS v(name, email, phone, company_name, source, status, user_email, deal_title)
JOIN users u ON u.email = v.user_email
LEFT JOIN deals d ON d.title = v.deal_title;

-- ── Activities ───────────────────────────────────────────────────────
INSERT INTO activities (type, description, occurred_at, deal_id, contact_id, logged_by_user_id, created_at, updated_at)
SELECT v.type, v.description, NOW() - v.days_ago * INTERVAL '1 day', d.id, NULL, u.id, NOW(), NOW()
FROM (VALUES
  ('CALL',    'Anka Yazılım ile lisans kapsamı hakkında ön görüşme yapıldı.', 'Anka Yazılım - Kurumsal Lisans Anlaşması', 'mehmet@salestrack.com', 12),
  ('MEETING', 'Teklif sunumu için yüz yüze toplantı gerçekleştirildi.',       'Anka Yazılım - Kurumsal Lisans Anlaşması', 'mehmet@salestrack.com', 4),
  ('EMAIL',   'Güncellenmiş teklif dokümanı e-posta ile iletildi.',          'Marmara Lojistik - Filo Takip Entegrasyonu', 'zeynep@salestrack.com', 6),
  ('NOTE',    'Müşteri bütçe onayını bekliyor, 2 hafta içinde dönüş yapacak.', 'Ege Tekstil - ERP Modernizasyonu', 'mehmet@salestrack.com', 2),
  ('MEETING', 'Sözleşme imza toplantısı tamamlandı.',                        'Boğaziçi Finans - Raporlama Platformu', 'zeynep@salestrack.com', 5)
) AS v(type, description, deal_title, user_email, days_ago)
JOIN deals d ON d.title = v.deal_title
JOIN users u ON u.email = v.user_email;

INSERT INTO activities (type, description, occurred_at, deal_id, contact_id, logged_by_user_id, created_at, updated_at)
SELECT 'CALL', 'İlk tanışma görüşmesi yapıldı, ihtiyaç analizi planlandı.', NOW() - INTERVAL '7 day', NULL, ct.id, u.id, NOW(), NOW()
FROM contacts ct
JOIN users u ON u.email = 'zeynep@salestrack.com'
WHERE ct.email = 'caner.yavuz@bogazicifinans.com';
