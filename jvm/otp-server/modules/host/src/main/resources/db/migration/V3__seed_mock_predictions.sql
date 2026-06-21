-- Mock predictions for testing the occupancy map endpoint
INSERT INTO users (id, email, display_name, status)
VALUES ('a0000000-0000-0000-0000-000000000001', 'test@otp.local', 'Test User', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO predictions (station_id, user_id, occupancy_level, type, created_at)
VALUES
    ('alameda_metro',       'a0000000-0000-0000-0000-000000000001', 3, 'COMPLETE', NOW() - INTERVAL '1 minute'),
    ('baixa_chiado_metro',  'a0000000-0000-0000-0000-000000000001', 4, 'COMPLETE', NOW() - INTERVAL '2 minutes'),
    ('cais_do_sodre_metro', 'a0000000-0000-0000-0000-000000000001', 2, 'COMPLETE', NOW() - INTERVAL '3 minutes'),
    ('oriente_metro',       'a0000000-0000-0000-0000-000000000001', 5, 'COMPLETE', NOW() - INTERVAL '1 minute'),
    ('rossio_metro',        'a0000000-0000-0000-0000-000000000001', 1, 'COMPLETE', NOW() - INTERVAL '5 minutes'),
    ('saldanha_metro',      'a0000000-0000-0000-0000-000000000001', 2, 'COMPLETE', NOW() - INTERVAL '4 minutes'),
    ('campo_grande_metro',  'a0000000-0000-0000-0000-000000000001', 3, 'COMPLETE', NOW() - INTERVAL '2 minutes'),
    ('terreiro_do_paco_metro', 'a0000000-0000-0000-0000-000000000001', 4, 'COMPLETE', NOW() - INTERVAL '3 minutes'),
    ('santa_apolonia_metro','a0000000-0000-0000-0000-000000000001', 1, 'COMPLETE', NOW() - INTERVAL '6 minutes'),
    ('jardim_zoologico_metro','a0000000-0000-0000-0000-000000000001', 5, 'COMPLETE', NOW() - INTERVAL '1 minute'),
    ('cais_do_sodre_train', 'a0000000-0000-0000-0000-000000000001', 3, 'COMPLETE', NOW() - INTERVAL '2 minutes'),
    ('cascais_train',       'a0000000-0000-0000-0000-000000000001', 2, 'COMPLETE', NOW() - INTERVAL '3 minutes'),
    ('sintra_train',        'a0000000-0000-0000-0000-000000000001', 4, 'COMPLETE', NOW() - INTERVAL '1 minute'),
    ('entrecampos_train',   'a0000000-0000-0000-0000-000000000001', 1, 'COMPLETE', NOW() - INTERVAL '5 minutes'),
    ('sete_rios_train',     'a0000000-0000-0000-0000-000000000001', 3, 'COMPLETE', NOW() - INTERVAL '4 minutes')
ON CONFLICT DO NOTHING;
