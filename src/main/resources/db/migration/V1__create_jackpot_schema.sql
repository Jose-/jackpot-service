create table jackpots (
    id uuid constraint pk_jackpots primary key,
    name varchar(120) not null,
    initial_pool_amount decimal(16,8) not null,
    current_pool_amount decimal(16,8) not null,
    contribution_strategy varchar(120) not null,
    contribution_parameters varchar(2000) not null,
    reward_strategy varchar(120) not null,
    reward_parameters varchar(2000) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    version bigint not null,
    constraint uk_jackpots_name unique (name),
    constraint ck_jackpot_pool check (
        initial_pool_amount > 0 and current_pool_amount >= initial_pool_amount
    )
);

create table bets (
    id uuid constraint pk_bets primary key,
    user_id uuid not null,
    jackpot_id uuid not null,
    bet_amount decimal(10,2) not null,
    status varchar(40) not null,
    failure_code varchar(120),
    failure_message varchar(500),
    failed_at timestamp with time zone,
    publication_attempts integer not null default 0,
    last_publication_error varchar(500),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_bets_jackpot foreign key (jackpot_id) references jackpots(id),
    constraint ck_bets_amount_positive check (bet_amount > 0),
    constraint ck_bets_status check (
        status in (
           'PENDING_PUBLICATION',
           'PUBLISHED',
           'CONTRIBUTED',
           'PUBLICATION_FAILED',
           'PROCESSING_FAILED'
        )
    )
);

create table jackpot_contributions (
    id uuid constraint pk_jackpot_contributions primary key,
    bet_id uuid not null,
    user_id uuid not null,
    jackpot_id uuid not null,
    stake_amount decimal(10,2) not null,
    contribution_amount decimal(16,8) not null,
    current_jackpot_amount decimal(16,8) not null,
    created_at timestamp with time zone not null,
    constraint uk_jackpot_contributions_bet unique (bet_id),
    constraint fk_contributions_bet foreign key (bet_id) references bets(id),
    constraint fk_contributions_jackpot foreign key (jackpot_id) references jackpots(id),
    constraint ck_contribution_stake_positive check (stake_amount > 0),
    constraint ck_contribution_amount_positive check (contribution_amount > 0),
    constraint ck_contribution_pool_positive check (current_jackpot_amount > 0)
);

create table jackpot_reward_evaluations (
    id uuid constraint pk_jackpot_reward_evaluations primary key,
    bet_id uuid not null,
    user_id uuid not null,
    jackpot_id uuid not null,
    won boolean not null,
    calculated_chance decimal(7,4) not null,
    generated_draw decimal(7,4) not null,
    reward_amount decimal(16,8),
    created_at timestamp with time zone not null,
    constraint uk_jackpot_reward_evaluations_bet unique (bet_id),
    constraint fk_evaluations_bet foreign key (bet_id) references bets(id),
    constraint fk_evaluations_jackpot foreign key (jackpot_id) references jackpots(id),
    constraint ck_evaluation_chance check (calculated_chance >= 0 and calculated_chance <= 100),
    constraint ck_evaluation_draw check (generated_draw >= 0 and generated_draw < 100),
    constraint ck_evaluation_reward check (
        (won = true and reward_amount > 0) or (won = false and reward_amount is null)
    )
);

create table jackpot_rewards (
    id uuid constraint pk_jackpot_rewards primary key,
    bet_id uuid not null,
    user_id uuid not null,
    jackpot_id uuid not null,
    jackpot_reward_amount decimal(16,8) not null,
    created_at timestamp with time zone not null,
    constraint uk_jackpot_rewards_bet unique (bet_id),
    constraint fk_rewards_bet foreign key (bet_id) references bets(id),
    constraint fk_rewards_jackpot foreign key (jackpot_id) references jackpots(id),
    constraint ck_reward_amount_positive check (jackpot_reward_amount > 0)
);

create index idx_bets_jackpot_id on bets(jackpot_id);
create index idx_contributions_jackpot_id on jackpot_contributions(jackpot_id);
create index idx_evaluations_jackpot_id on jackpot_reward_evaluations(jackpot_id);
create index idx_rewards_jackpot_id on jackpot_rewards(jackpot_id);
