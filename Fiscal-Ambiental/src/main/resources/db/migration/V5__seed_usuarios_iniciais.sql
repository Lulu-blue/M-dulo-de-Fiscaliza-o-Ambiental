INSERT INTO usuarios (id, nome, email, senha_hash, cargo, cpf, matricula, ativo)
VALUES 
(gen_random_uuid(), 'Gestor Geral', 'gestor@semac.mg.gov.br', '$2b$12$8AHc1iJW1kbXy6PSfHGupuvo90mVSMRQNLL7rKNRhBW0wXikqPG92', 'GESTOR', '00000000000', 'M-001', true),
(gen_random_uuid(), 'Fiscal Carlos', 'carlos@semac.mg.gov.br', '$2b$12$8AHc1iJW1kbXy6PSfHGupuvo90mVSMRQNLL7rKNRhBW0wXikqPG92', 'FISCAL', '11111111111', 'M-002', true),
(gen_random_uuid(), 'Fiscal Mariana', 'mariana@semac.mg.gov.br', '$2b$12$8AHc1iJW1kbXy6PSfHGupuvo90mVSMRQNLL7rKNRhBW0wXikqPG92', 'FISCAL', '22222222222', 'M-003', true)
ON CONFLICT (email) DO NOTHING;
