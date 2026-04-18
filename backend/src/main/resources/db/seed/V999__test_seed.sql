-- V999: Seed danych testowych — 64 ekipy remontowe (tylko profil local)
-- Hasło dla wszystkich kont: Test1234!
-- BCrypt(10) hash: $2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m

-- ============================================================
-- KROK 1: Użytkownicy CREW
-- ============================================================

INSERT INTO users (email, password_hash, role, email_verified) VALUES
-- MAZOWIECKIE
('ekipa01@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa02@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa03@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa04@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- MALOPOLSKIE
('ekipa05@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa06@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa07@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa08@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- DOLNOSLASKIE
('ekipa09@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa10@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa11@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa12@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- POMORSKIE
('ekipa13@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa14@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa15@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa16@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- WIELKOPOLSKIE
('ekipa17@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa18@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa19@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa20@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- LODZKIE
('ekipa21@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa22@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa23@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa24@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- SLASKIE
('ekipa25@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa26@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa27@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa28@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- LUBELSKIE
('ekipa29@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa30@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa31@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa32@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- PODKARPACKIE
('ekipa33@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa34@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa35@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa36@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- PODLASKIE
('ekipa37@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa38@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa39@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa40@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- KUJAWSKO_POMORSKIE
('ekipa41@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa42@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa43@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa44@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- WARMINSKO_MAZURSKIE
('ekipa45@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa46@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa47@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa48@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- SWIETOKRZYSKIE
('ekipa49@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa50@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa51@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa52@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- LUBUSKIE
('ekipa53@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa54@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa55@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa56@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- OPOLSKIE
('ekipa57@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa58@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa59@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa60@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
-- ZACHODNIOPOMORSKIE
('ekipa61@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa62@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa63@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true),
('ekipa64@seed.test', '$2a$10$xozNrYaQS0mVH4shhaZ8geS5NuKhh2Y9p4HSH6pi3VtCvt.b4Xm.m', 'CREW', true);

-- ============================================================
-- KROK 2: Profile ekip
-- Pola: user_id, company_name, slug, description, phone, contact_email,
--       city, voivodeship, service_radius_km, nip, is_visible
-- ============================================================

INSERT INTO crew_profiles (user_id, company_name, slug, description, phone, contact_email, city, voivodeship, service_radius_km, nip, is_visible)
VALUES
-- MAZOWIECKIE
((SELECT id FROM users WHERE email = 'ekipa01@seed.test'), 'Budrem Warszawa', 'budrem-warszawa', 'Kompleksowe remonty mieszkań i domów w Warszawie i okolicach.', '600100001', 'kontakt@budrem-warszawa.pl', 'Warszawa', 'MAZOWIECKIE', 50, '5210001001', true),
((SELECT id FROM users WHERE email = 'ekipa02@seed.test'), 'Pro Instalacje Warszawa', 'pro-instalacje-warszawa', 'Instalacje gazowe, ogrzewanie i klimatyzacja na najwyższym poziomie.', '600100002', 'kontakt@pro-instalacje.pl', 'Warszawa', 'MAZOWIECKIE', 40, '5210001002', true),
((SELECT id FROM users WHERE email = 'ekipa03@seed.test'), 'Radom Remonty', 'radom-remonty', 'Profesjonalne malowanie i tynkowanie — Radom i powiat.', '600100003', 'kontakt@radom-remonty.pl', 'Radom', 'MAZOWIECKIE', 35, '7990001003', true),
((SELECT id FROM users WHERE email = 'ekipa04@seed.test'), 'Płock Fachowcy', 'plock-fachowcy', 'Glazura, łazienki i kuchnie — Płock i okolice.', '600100004', 'kontakt@plock-fachowcy.pl', 'Płock', 'MAZOWIECKIE', 40, '7740001004', true),
-- MALOPOLSKIE
((SELECT id FROM users WHERE email = 'ekipa05@seed.test'), 'Kowalski Usługi Budowlane', 'kowalski-krakow', 'Malowanie, tynkowanie i elewacje — Kraków i Małopolska.', '600100005', 'kontakt@kowalski-krakow.pl', 'Kraków', 'MALOPOLSKIE', 60, '6790001005', true),
((SELECT id FROM users WHERE email = 'ekipa06@seed.test'), 'Pro Remont Kraków', 'pro-remont-krakow', 'Łazienki, kuchnie, podłogi — kompleksowe wykończenia wnętrz.', '600100006', 'kontakt@pro-remont-krakow.pl', 'Kraków', 'MALOPOLSKIE', 50, '6790001006', true),
((SELECT id FROM users WHERE email = 'ekipa07@seed.test'), 'Tarnów Budownictwo', 'tarnow-budownictwo', 'Dachy, ocieplenia, elewacje — solidnie i terminowo.', '600100007', 'kontakt@tarnow-budownictwo.pl', 'Tarnów', 'MALOPOLSKIE', 45, '8730001007', true),
((SELECT id FROM users WHERE email = 'ekipa08@seed.test'), 'Nowak Instalacje', 'nowak-instalacje-nowy-sacz', 'Hydraulika i instalacje gazowe — Nowy Sącz i region.', '600100008', 'kontakt@nowak-instalacje.pl', 'Nowy Sącz', 'MALOPOLSKIE', 50, '7340001008', true),
-- DOLNOSLASKIE
((SELECT id FROM users WHERE email = 'ekipa09@seed.test'), 'Wrocław Remonty Express', 'wroclaw-remonty-express', 'Szybkie i rzetelne remonty we Wrocławiu.', '600100009', 'kontakt@wroclaw-express.pl', 'Wrocław', 'DOLNOSLASKIE', 55, '8990001009', true),
((SELECT id FROM users WHERE email = 'ekipa10@seed.test'), 'Fachowa Ekipa Wrocław', 'fachowa-ekipa-wroclaw', 'Dachy, ocieplenia i elewacje we Wrocławiu i okolicy.', '600100010', 'kontakt@fachowa-ekipa.pl', 'Wrocław', 'DOLNOSLASKIE', 60, '8990001010', true),
((SELECT id FROM users WHERE email = 'ekipa11@seed.test'), 'Legnica Budownictwo', 'legnica-budownictwo', 'Glazura, podłogi, łazienki — Legnica i powiat legnicki.', '600100011', 'kontakt@legnica-bud.pl', 'Legnica', 'DOLNOSLASKIE', 40, '6910001011', true),
((SELECT id FROM users WHERE email = 'ekipa12@seed.test'), 'Wałbrzych Remonty', 'walbrzych-remonty', 'Elektryka, ogrzewanie i klimatyzacja — Wałbrzych.', '600100012', 'kontakt@walbrzych-remonty.pl', 'Wałbrzych', 'DOLNOSLASKIE', 40, '8860001012', true),
-- POMORSKIE
((SELECT id FROM users WHERE email = 'ekipa13@seed.test'), 'Trójmiasto Remonty', 'trojmiasto-remonty', 'Hydraulika, elektryka i malowanie — Gdańsk, Gdynia, Sopot.', '600100013', 'kontakt@trojmiasto-remonty.pl', 'Gdańsk', 'POMORSKIE', 50, '5830001013', true),
((SELECT id FROM users WHERE email = 'ekipa14@seed.test'), 'Gdańsk Profesjonalni', 'gdansk-profesjonalni', 'Łazienki, kuchnie, glazura — profesjonalne wykończenia.', '600100014', 'kontakt@gdansk-profesjonalni.pl', 'Gdańsk', 'POMORSKIE', 40, '5830001014', true),
((SELECT id FROM users WHERE email = 'ekipa15@seed.test'), 'Gdynia Fachowcy', 'gdynia-fachowcy', 'Dachy, ocieplenia i stolarka — Gdynia i Trójmiasto.', '600100015', 'kontakt@gdynia-fachowcy.pl', 'Gdynia', 'POMORSKIE', 45, '5860001015', true),
((SELECT id FROM users WHERE email = 'ekipa16@seed.test'), 'Słupsk Remonty', 'slupsk-remonty', 'Kompleksowe remonty — Słupsk i okolice.', '600100016', 'kontakt@slupsk-remonty.pl', 'Słupsk', 'POMORSKIE', 50, '8390001016', true),
-- WIELKOPOLSKIE
((SELECT id FROM users WHERE email = 'ekipa17@seed.test'), 'Poznań Remont Pro', 'poznan-remont-pro', 'Malowanie, tynkowanie, elewacje — Poznań i Wielkopolska.', '600100017', 'kontakt@poznan-remont-pro.pl', 'Poznań', 'WIELKOPOLSKIE', 60, '7820001017', true),
((SELECT id FROM users WHERE email = 'ekipa18@seed.test'), 'Zielińscy Usługi Budowlane', 'zielinscy-uslugi-poznan', 'Hydraulika, instalacje gazowe i ogrzewanie — Poznań.', '600100018', 'kontakt@zielinscy-uslugi.pl', 'Poznań', 'WIELKOPOLSKIE', 50, '7820001018', true),
((SELECT id FROM users WHERE email = 'ekipa19@seed.test'), 'Kalisz Budowlani', 'kalisz-budowlani', 'Glazura, łazienki i podłogi — Kalisz i powiat.', '600100019', 'kontakt@kalisz-budowlani.pl', 'Kalisz', 'WIELKOPOLSKIE', 40, '6220001019', true),
((SELECT id FROM users WHERE email = 'ekipa20@seed.test'), 'Konin Remonty', 'konin-remonty', 'Stolarka, ogrodzenia, remonty generalne — Konin.', '600100020', 'kontakt@konin-remonty.pl', 'Konin', 'WIELKOPOLSKIE', 45, '6620001020', true),
-- LODZKIE
((SELECT id FROM users WHERE email = 'ekipa21@seed.test'), 'Łódź Remonty Szybko', 'lodz-remonty-szybko', 'Malowanie, tynkowanie, prace wykończeniowe — Łódź.', '600100021', 'kontakt@lodz-remonty.pl', 'Łódź', 'LODZKIE', 50, '7250001021', true),
((SELECT id FROM users WHERE email = 'ekipa22@seed.test'), 'Ekipa Łódź', 'ekipa-lodz', 'Elektryka, instalacje gazowe, klimatyzacja — Łódź i okolice.', '600100022', 'kontakt@ekipa-lodz.pl', 'Łódź', 'LODZKIE', 55, '7250001022', true),
((SELECT id FROM users WHERE email = 'ekipa23@seed.test'), 'Piotrków Remont', 'piotrkow-remont', 'Hydraulika, łazienki i kuchnie — Piotrków Trybunalski.', '600100023', 'kontakt@piotrkow-remont.pl', 'Piotrków Trybunalski', 'LODZKIE', 40, '7720001023', true),
((SELECT id FROM users WHERE email = 'ekipa24@seed.test'), 'Skierniewice Budowlani', 'skierniewice-budowlani', 'Dachy, elewacje, ocieplenia — Skierniewice i region.', '600100024', 'kontakt@skierniewice-bud.pl', 'Skierniewice', 'LODZKIE', 40, '8360001024', true),
-- SLASKIE
((SELECT id FROM users WHERE email = 'ekipa25@seed.test'), 'Katowice Pro Remont', 'katowice-pro-remont', 'Remonty generalne, malowanie, tynkowanie — Katowice.', '600100025', 'kontakt@katowice-pro.pl', 'Katowice', 'SLASKIE', 50, '6340001025', true),
((SELECT id FROM users WHERE email = 'ekipa26@seed.test'), 'Śląska Ekipa Budowlana', 'slaska-ekipa-budowlana', 'Glazura, podłogi i łazienki — Katowice i aglomeracja śląska.', '600100026', 'kontakt@slaska-ekipa.pl', 'Katowice', 'SLASKIE', 60, '6340001026', true),
((SELECT id FROM users WHERE email = 'ekipa27@seed.test'), 'Gliwice Fachowcy', 'gliwice-fachowcy', 'Dachy, ocieplenia i stolarka — Gliwice i okolice.', '600100027', 'kontakt@gliwice-fachowcy.pl', 'Gliwice', 'SLASKIE', 40, '4440001027', true),
((SELECT id FROM users WHERE email = 'ekipa28@seed.test'), 'Bytom Remonty', 'bytom-remonty', 'Elektryka, ogrzewanie i instalacje gazowe — Bytom.', '600100028', 'kontakt@bytom-remonty.pl', 'Bytom', 'SLASKIE', 40, '6260001028', true),
-- LUBELSKIE
((SELECT id FROM users WHERE email = 'ekipa29@seed.test'), 'Lublin Remont Expert', 'lublin-remont-expert', 'Hydraulika, elektryka, remonty generalne — Lublin.', '600100029', 'kontakt@lublin-expert.pl', 'Lublin', 'LUBELSKIE', 55, '7120001029', true),
((SELECT id FROM users WHERE email = 'ekipa30@seed.test'), 'Pro Remonty Lublin', 'pro-remonty-lublin', 'Malowanie, elewacje, prace wykończeniowe — Lublin i okolice.', '600100030', 'kontakt@pro-remonty-lublin.pl', 'Lublin', 'LUBELSKIE', 50, '7120001030', true),
((SELECT id FROM users WHERE email = 'ekipa31@seed.test'), 'Zamość Budownictwo', 'zamosc-budownictwo', 'Glazura, łazienki, kuchnie — Zamość i Roztocze.', '600100031', 'kontakt@zamosc-bud.pl', 'Zamość', 'LUBELSKIE', 45, '9220001031', true),
((SELECT id FROM users WHERE email = 'ekipa32@seed.test'), 'Chełm Usługi Budowlane', 'chelm-uslugi-budowlane', 'Dachy, ocieplenia, ogrodzenia — Chełm i powiat.', '600100032', 'kontakt@chelm-uslugi.pl', 'Chełm', 'LUBELSKIE', 40, '5630001032', true),
-- PODKARPACKIE
((SELECT id FROM users WHERE email = 'ekipa33@seed.test'), 'Rzeszów Remonty Pro', 'rzeszow-remonty-pro', 'Malowanie, tynkowanie, elewacje — Rzeszów i Podkarpacie.', '600100033', 'kontakt@rzeszow-remonty.pl', 'Rzeszów', 'PODKARPACKIE', 60, '8130001033', true),
((SELECT id FROM users WHERE email = 'ekipa34@seed.test'), 'Ekipa Rzeszów', 'ekipa-rzeszow', 'Hydraulika, instalacje gazowe i łazienki — Rzeszów.', '600100034', 'kontakt@ekipa-rzeszow.pl', 'Rzeszów', 'PODKARPACKIE', 50, '8130001034', true),
((SELECT id FROM users WHERE email = 'ekipa35@seed.test'), 'Przemyśl Budowlani', 'przemysl-budowlani', 'Remonty generalne, prace wykończeniowe — Przemyśl.', '600100035', 'kontakt@przemysl-bud.pl', 'Przemyśl', 'PODKARPACKIE', 45, '7950001035', true),
((SELECT id FROM users WHERE email = 'ekipa36@seed.test'), 'Krosno Remonty', 'krosno-remonty', 'Dachy, stolarka, ocieplenia — Krosno i Beskid Niski.', '600100036', 'kontakt@krosno-remonty.pl', 'Krosno', 'PODKARPACKIE', 50, '3700001036', true),
-- PODLASKIE
((SELECT id FROM users WHERE email = 'ekipa37@seed.test'), 'Białystok Fachowcy', 'bialystok-fachowcy', 'Elektryka, ogrzewanie, klimatyzacja — Białystok.', '600100037', 'kontakt@bialystok-fachowcy.pl', 'Białystok', 'PODLASKIE', 55, '9660001037', true),
((SELECT id FROM users WHERE email = 'ekipa38@seed.test'), 'Remont Białystok', 'remont-bialystok', 'Malowanie, tynkowanie, prace wykończeniowe — Białystok i okolice.', '600100038', 'kontakt@remont-bialystok.pl', 'Białystok', 'PODLASKIE', 50, '9660001038', true),
((SELECT id FROM users WHERE email = 'ekipa39@seed.test'), 'Suwałki Budowlani', 'suwalki-budowlani', 'Hydraulika i instalacje gazowe — Suwałki i Suwalszczyzna.', '600100039', 'kontakt@suwalki-bud.pl', 'Suwałki', 'PODLASKIE', 50, '8440001039', true),
((SELECT id FROM users WHERE email = 'ekipa40@seed.test'), 'Łomża Remonty', 'lomza-remonty', 'Dachy, elewacje, ocieplenia — Łomża i Podlasie.', '600100040', 'kontakt@lomza-remonty.pl', 'Łomża', 'PODLASKIE', 45, '7180001040', true),
-- KUJAWSKO_POMORSKIE
((SELECT id FROM users WHERE email = 'ekipa41@seed.test'), 'Bydgoszcz Remonty', 'bydgoszcz-remonty', 'Remonty generalne, malowanie — Bydgoszcz i Kujawsko-Pomorskie.', '600100041', 'kontakt@bydgoszcz-remonty.pl', 'Bydgoszcz', 'KUJAWSKO_POMORSKIE', 55, '5540001041', true),
((SELECT id FROM users WHERE email = 'ekipa42@seed.test'), 'Fachowcy Bydgoszcz', 'fachowcy-bydgoszcz', 'Glazura, łazienki, podłogi — Bydgoszcz.', '600100042', 'kontakt@fachowcy-bydgoszcz.pl', 'Bydgoszcz', 'KUJAWSKO_POMORSKIE', 50, '5540001042', true),
((SELECT id FROM users WHERE email = 'ekipa43@seed.test'), 'Toruń Budownictwo', 'torun-budownictwo', 'Hydraulika, elektryka i instalacje gazowe — Toruń.', '600100043', 'kontakt@torun-bud.pl', 'Toruń', 'KUJAWSKO_POMORSKIE', 45, '8790001043', true),
((SELECT id FROM users WHERE email = 'ekipa44@seed.test'), 'Włocławek Remonty', 'wloclawek-remonty', 'Dachy, ocieplenia, elewacje — Włocławek i Kujawy.', '600100044', 'kontakt@wloclawek-remonty.pl', 'Włocławek', 'KUJAWSKO_POMORSKIE', 45, '9100001044', true),
-- WARMINSKO_MAZURSKIE
((SELECT id FROM users WHERE email = 'ekipa45@seed.test'), 'Olsztyn Remont Pro', 'olsztyn-remont-pro', 'Malowanie, tynkowanie, prace wykończeniowe — Olsztyn.', '600100045', 'kontakt@olsztyn-remont.pl', 'Olsztyn', 'WARMINSKO_MAZURSKIE', 55, '7390001045', true),
((SELECT id FROM users WHERE email = 'ekipa46@seed.test'), 'Ekipa Olsztyn', 'ekipa-olsztyn', 'Hydraulika, łazienki, kuchnie — Olsztyn i Mazury.', '600100046', 'kontakt@ekipa-olsztyn.pl', 'Olsztyn', 'WARMINSKO_MAZURSKIE', 60, '7390001046', true),
((SELECT id FROM users WHERE email = 'ekipa47@seed.test'), 'Elbląg Budowlani', 'elblag-budowlani', 'Elektryka, ogrzewanie, stolarka — Elbląg i okolice.', '600100047', 'kontakt@elblag-bud.pl', 'Elbląg', 'WARMINSKO_MAZURSKIE', 40, '8200001047', true),
((SELECT id FROM users WHERE email = 'ekipa48@seed.test'), 'Ostróda Remonty', 'ostroda-remonty', 'Remonty generalne, elewacje, ocieplenia — Ostróda.', '600100048', 'kontakt@ostroda-remonty.pl', 'Ostróda', 'WARMINSKO_MAZURSKIE', 45, '8990001048', true),
-- SWIETOKRZYSKIE
((SELECT id FROM users WHERE email = 'ekipa49@seed.test'), 'Kielce Remonty', 'kielce-remonty', 'Malowanie, tynkowanie, glazura — Kielce i region świętokrzyski.', '600100049', 'kontakt@kielce-remonty.pl', 'Kielce', 'SWIETOKRZYSKIE', 50, '6570001049', true),
((SELECT id FROM users WHERE email = 'ekipa50@seed.test'), 'Pro Remont Kielce', 'pro-remont-kielce', 'Dachy, ocieplenia, elewacje — Kielce.', '600100050', 'kontakt@pro-remont-kielce.pl', 'Kielce', 'SWIETOKRZYSKIE', 55, '6570001050', true),
((SELECT id FROM users WHERE email = 'ekipa51@seed.test'), 'Ostrowiec Budownictwo', 'ostrowiec-budownictwo', 'Hydraulika i instalacje gazowe — Ostrowiec Świętokrzyski.', '600100051', 'kontakt@ostrowiec-bud.pl', 'Ostrowiec Świętokrzyski', 'SWIETOKRZYSKIE', 40, '2920001051', true),
((SELECT id FROM users WHERE email = 'ekipa52@seed.test'), 'Starachowice Fachowcy', 'starachowice-fachowcy', 'Łazienki, kuchnie, podłogi — Starachowice i powiat.', '600100052', 'kontakt@starachowice-fachowcy.pl', 'Starachowice', 'SWIETOKRZYSKIE', 40, '2720001052', true),
-- LUBUSKIE
((SELECT id FROM users WHERE email = 'ekipa53@seed.test'), 'Zielona Góra Remonty', 'zielona-gora-remonty', 'Elektryka, klimatyzacja, ogrzewanie — Zielona Góra.', '600100053', 'kontakt@zielona-gora-remonty.pl', 'Zielona Góra', 'LUBUSKIE', 45, '9280001053', true),
((SELECT id FROM users WHERE email = 'ekipa54@seed.test'), 'Lubuska Ekipa', 'lubuska-ekipa', 'Malowanie, tynkowanie, elewacje — Zielona Góra i Lubuskie.', '600100054', 'kontakt@lubuska-ekipa.pl', 'Zielona Góra', 'LUBUSKIE', 55, '9280001054', true),
((SELECT id FROM users WHERE email = 'ekipa55@seed.test'), 'Gorzów Budownictwo', 'gorzow-budownictwo', 'Remonty generalne, prace wykończeniowe — Gorzów Wielkopolski.', '600100055', 'kontakt@gorzow-bud.pl', 'Gorzów Wielkopolski', 'LUBUSKIE', 50, '6660001055', true),
((SELECT id FROM users WHERE email = 'ekipa56@seed.test'), 'Żary Remonty', 'zary-remonty', 'Hydraulika, glazura, łazienki — Żary i powiat żarski.', '600100056', 'kontakt@zary-remonty.pl', 'Żary', 'LUBUSKIE', 40, '6860001056', true),
-- OPOLSKIE
((SELECT id FROM users WHERE email = 'ekipa57@seed.test'), 'Opole Remont Pro', 'opole-remont-pro', 'Malowanie, tynkowanie, remonty generalne — Opole i Opolskie.', '600100057', 'kontakt@opole-remont.pl', 'Opole', 'OPOLSKIE', 50, '7540001057', true),
((SELECT id FROM users WHERE email = 'ekipa58@seed.test'), 'Śląskie Remonty Opole', 'slaskie-remonty-opole', 'Dachy, ocieplenia, elewacje — Opole.', '600100058', 'kontakt@slaskie-remonty-opole.pl', 'Opole', 'OPOLSKIE', 60, '7540001058', true),
((SELECT id FROM users WHERE email = 'ekipa59@seed.test'), 'Kędzierzyn Fachowcy', 'kedzierzyn-fachowcy', 'Elektryka i instalacje gazowe — Kędzierzyn-Koźle.', '600100059', 'kontakt@kedzierzyn-fachowcy.pl', 'Kędzierzyn-Koźle', 'OPOLSKIE', 40, '4760001059', true),
((SELECT id FROM users WHERE email = 'ekipa60@seed.test'), 'Nysa Budownictwo', 'nysa-budownictwo', 'Glazura, podłogi, łazienki — Nysa i ziemia nyska.', '600100060', 'kontakt@nysa-bud.pl', 'Nysa', 'OPOLSKIE', 45, '4830001060', true),
-- ZACHODNIOPOMORSKIE
((SELECT id FROM users WHERE email = 'ekipa61@seed.test'), 'Szczecin Remonty Express', 'szczecin-remonty-express', 'Szybkie remonty generalne — Szczecin i aglomeracja.', '600100061', 'kontakt@szczecin-express.pl', 'Szczecin', 'ZACHODNIOPOMORSKIE', 55, '8120001061', true),
((SELECT id FROM users WHERE email = 'ekipa62@seed.test'), 'Fachowcy Szczecin', 'fachowcy-szczecin', 'Hydraulika, elektryka, instalacje gazowe — Szczecin.', '600100062', 'kontakt@fachowcy-szczecin.pl', 'Szczecin', 'ZACHODNIOPOMORSKIE', 50, '8120001062', true),
((SELECT id FROM users WHERE email = 'ekipa63@seed.test'), 'Koszalin Budownictwo', 'koszalin-budownictwo', 'Dachy, ocieplenia, stolarka — Koszalin i Wybrzeże.', '600100063', 'kontakt@koszalin-bud.pl', 'Koszalin', 'ZACHODNIOPOMORSKIE', 50, '7500001063', true),
((SELECT id FROM users WHERE email = 'ekipa64@seed.test'), 'Świnoujście Remonty', 'swinoujscie-remonty', 'Łazienki, kuchnie i ogrodzenia — Świnoujście.', '600100064', 'kontakt@swinoujscie-remonty.pl', 'Świnoujście', 'ZACHODNIOPOMORSKIE', 40, '3270001064', true);

-- ============================================================
-- KROK 3: Kategorie usług (crew_services)
-- ============================================================

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'budrem-warszawa'            AND c.slug IN ('hydraulika', 'elektryka', 'remonty-generalne');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'pro-instalacje-warszawa'    AND c.slug IN ('instalacje-gazowe', 'ogrzewanie', 'klimatyzacja');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'radom-remonty'              AND c.slug IN ('malowanie', 'tynkowanie', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'plock-fachowcy'             AND c.slug IN ('glazura', 'lazienki', 'kuchnie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'kowalski-krakow'            AND c.slug IN ('malowanie', 'tynkowanie', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'pro-remont-krakow'          AND c.slug IN ('lazienki', 'kuchnie', 'podlogi');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'tarnow-budownictwo'         AND c.slug IN ('dachy', 'ocieplenia', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'nowak-instalacje-nowy-sacz' AND c.slug IN ('hydraulika', 'instalacje-gazowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'wroclaw-remonty-express'    AND c.slug IN ('remonty-generalne', 'malowanie', 'tynkowanie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'fachowa-ekipa-wroclaw'      AND c.slug IN ('dachy', 'ocieplenia', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'legnica-budownictwo'        AND c.slug IN ('glazura', 'podlogi', 'lazienki');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'walbrzych-remonty'          AND c.slug IN ('elektryka', 'ogrzewanie', 'klimatyzacja');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'trojmiasto-remonty'         AND c.slug IN ('hydraulika', 'elektryka', 'malowanie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'gdansk-profesjonalni'       AND c.slug IN ('lazienki', 'kuchnie', 'glazura');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'gdynia-fachowcy'            AND c.slug IN ('dachy', 'ocieplenia', 'stolarka');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'slupsk-remonty'             AND c.slug IN ('remonty-generalne', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'poznan-remont-pro'          AND c.slug IN ('malowanie', 'tynkowanie', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'zielinscy-uslugi-poznan'    AND c.slug IN ('hydraulika', 'instalacje-gazowe', 'ogrzewanie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'kalisz-budowlani'           AND c.slug IN ('glazura', 'lazienki', 'podlogi');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'konin-remonty'              AND c.slug IN ('stolarka', 'ogrodzenia', 'remonty-generalne');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'lodz-remonty-szybko'        AND c.slug IN ('malowanie', 'tynkowanie', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'ekipa-lodz'                 AND c.slug IN ('elektryka', 'instalacje-gazowe', 'klimatyzacja');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'piotrkow-remont'            AND c.slug IN ('hydraulika', 'lazienki', 'kuchnie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'skierniewice-budowlani'     AND c.slug IN ('dachy', 'elewacje', 'ocieplenia');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'katowice-pro-remont'        AND c.slug IN ('remonty-generalne', 'malowanie', 'tynkowanie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'slaska-ekipa-budowlana'     AND c.slug IN ('glazura', 'podlogi', 'lazienki');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'gliwice-fachowcy'           AND c.slug IN ('dachy', 'ocieplenia', 'stolarka');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'bytom-remonty'              AND c.slug IN ('elektryka', 'ogrzewanie', 'instalacje-gazowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'lublin-remont-expert'       AND c.slug IN ('hydraulika', 'elektryka', 'remonty-generalne');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'pro-remonty-lublin'         AND c.slug IN ('malowanie', 'elewacje', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'zamosc-budownictwo'         AND c.slug IN ('glazura', 'lazienki', 'kuchnie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'chelm-uslugi-budowlane'     AND c.slug IN ('dachy', 'ocieplenia', 'ogrodzenia');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'rzeszow-remonty-pro'        AND c.slug IN ('malowanie', 'tynkowanie', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'ekipa-rzeszow'              AND c.slug IN ('hydraulika', 'instalacje-gazowe', 'lazienki');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'przemysl-budowlani'         AND c.slug IN ('remonty-generalne', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'krosno-remonty'             AND c.slug IN ('dachy', 'stolarka', 'ocieplenia');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'bialystok-fachowcy'         AND c.slug IN ('elektryka', 'ogrzewanie', 'klimatyzacja');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'remont-bialystok'           AND c.slug IN ('malowanie', 'tynkowanie', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'suwalki-budowlani'          AND c.slug IN ('hydraulika', 'instalacje-gazowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'lomza-remonty'              AND c.slug IN ('dachy', 'elewacje', 'ocieplenia');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'bydgoszcz-remonty'          AND c.slug IN ('remonty-generalne', 'malowanie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'fachowcy-bydgoszcz'         AND c.slug IN ('glazura', 'lazienki', 'podlogi');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'torun-budownictwo'          AND c.slug IN ('hydraulika', 'elektryka', 'instalacje-gazowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'wloclawek-remonty'          AND c.slug IN ('dachy', 'ocieplenia', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'olsztyn-remont-pro'         AND c.slug IN ('malowanie', 'tynkowanie', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'ekipa-olsztyn'              AND c.slug IN ('hydraulika', 'lazienki', 'kuchnie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'elblag-budowlani'           AND c.slug IN ('elektryka', 'ogrzewanie', 'stolarka');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'ostroda-remonty'            AND c.slug IN ('remonty-generalne', 'elewacje', 'ocieplenia');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'kielce-remonty'             AND c.slug IN ('malowanie', 'tynkowanie', 'glazura');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'pro-remont-kielce'          AND c.slug IN ('dachy', 'ocieplenia', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'ostrowiec-budownictwo'      AND c.slug IN ('hydraulika', 'instalacje-gazowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'starachowice-fachowcy'      AND c.slug IN ('lazienki', 'kuchnie', 'podlogi');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'zielona-gora-remonty'       AND c.slug IN ('elektryka', 'klimatyzacja', 'ogrzewanie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'lubuska-ekipa'              AND c.slug IN ('malowanie', 'tynkowanie', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'gorzow-budownictwo'         AND c.slug IN ('remonty-generalne', 'prace-wykonczeniowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'zary-remonty'               AND c.slug IN ('hydraulika', 'glazura', 'lazienki');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'opole-remont-pro'           AND c.slug IN ('malowanie', 'tynkowanie', 'remonty-generalne');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'slaskie-remonty-opole'      AND c.slug IN ('dachy', 'ocieplenia', 'elewacje');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'kedzierzyn-fachowcy'        AND c.slug IN ('elektryka', 'instalacje-gazowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'nysa-budownictwo'           AND c.slug IN ('glazura', 'podlogi', 'lazienki');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'szczecin-remonty-express'   AND c.slug IN ('remonty-generalne', 'malowanie', 'tynkowanie');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'fachowcy-szczecin'          AND c.slug IN ('hydraulika', 'elektryka', 'instalacje-gazowe');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'koszalin-budownictwo'       AND c.slug IN ('dachy', 'ocieplenia', 'stolarka');

INSERT INTO crew_services (crew_profile_id, category_id)
SELECT p.id, c.id FROM crew_profiles p, service_categories c
WHERE p.slug = 'swinoujscie-remonty'        AND c.slug IN ('lazienki', 'kuchnie', 'ogrodzenia');
