-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Organizations table
CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    domain VARCHAR(255) UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    plan_type VARCHAR(50) NOT NULL DEFAULT 'FREE',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    max_users INTEGER,
    max_projects INTEGER,
    max_storage_gb INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

CREATE INDEX idx_org_domain ON organizations(domain);
CREATE INDEX idx_org_deleted ON organizations(is_deleted);

-- Organization settings
CREATE TABLE organization_settings (
    organization_id UUID NOT NULL REFERENCES organizations(id),
    setting_key VARCHAR(255) NOT NULL,
    setting_value TEXT,
    PRIMARY KEY (organization_id, setting_key)
);

-- Organization features
CREATE TABLE organization_features (
    organization_id UUID NOT NULL REFERENCES organizations(id),
    feature VARCHAR(100) NOT NULL,
    PRIMARY KEY (organization_id, feature)
);

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(255),
    avatar_url VARCHAR(500),
    phone_number VARCHAR(50),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'en_US',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    password_hash VARCHAR(255),
    last_login_at TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_org_username UNIQUE (organization_id, username)
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_org ON users(organization_id);
CREATE INDEX idx_user_deleted ON users(is_deleted);

-- User roles
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- User preferences
CREATE TABLE user_preferences (
    user_id UUID NOT NULL REFERENCES users(id),
    preference_key VARCHAR(255) NOT NULL,
    preference_value TEXT,
    PRIMARY KEY (user_id, preference_key)
);

-- Workspaces table
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    icon VARCHAR(50),
    visibility VARCHAR(50) NOT NULL DEFAULT 'PRIVATE',
    organization_id UUID NOT NULL REFERENCES organizations(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

CREATE INDEX idx_workspace_org ON workspaces(organization_id);
CREATE INDEX idx_workspace_deleted ON workspaces(is_deleted);

-- Workspace members
CREATE TABLE workspace_members (
    workspace_id UUID NOT NULL REFERENCES workspaces(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (workspace_id, user_id)
);

-- Teams table
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    icon VARCHAR(50),
    workspace_id UUID NOT NULL REFERENCES workspaces(id),
    team_lead_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

CREATE INDEX idx_team_workspace ON teams(workspace_id);
CREATE INDEX idx_team_deleted ON teams(is_deleted);

-- Team members
CREATE TABLE team_members (
    team_id UUID NOT NULL REFERENCES teams(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (team_id, user_id)
);

-- Portfolios table
CREATE TABLE portfolios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    owner_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- Projects table
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    icon VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    default_view VARCHAR(50) DEFAULT 'LIST',
    start_date DATE,
    due_date DATE,
    progress_percentage INTEGER DEFAULT 0,
    workspace_id UUID NOT NULL REFERENCES workspaces(id),
    owner_id UUID REFERENCES users(id),
    team_id UUID REFERENCES teams(id),
    portfolio_id UUID REFERENCES portfolios(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

CREATE INDEX idx_project_workspace ON projects(workspace_id);
CREATE INDEX idx_project_owner ON projects(owner_id);
CREATE INDEX idx_project_deleted ON projects(is_deleted);
CREATE INDEX idx_project_status ON projects(status);

-- Project members
CREATE TABLE project_members (
    project_id UUID NOT NULL REFERENCES projects(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (project_id, user_id)
);

-- Sections table
CREATE TABLE sections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER,
    project_id UUID NOT NULL REFERENCES projects(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- Tasks table
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    task_number BIGSERIAL NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    start_date DATE,
    due_date DATE,
    completed_at TIMESTAMP,
    estimated_hours DECIMAL(10,2),
    actual_hours DECIMAL(10,2),
    progress_percentage INTEGER DEFAULT 0,
    story_points INTEGER,
    section_id UUID REFERENCES sections(id),
    assignee_id UUID REFERENCES users(id),
    created_by_user_id UUID REFERENCES users(id),
    parent_task_id UUID REFERENCES tasks(id),
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_task_template_id UUID REFERENCES tasks(id),
    recurrence_type VARCHAR(50),
    recurrence_interval INTEGER,
    recurrence_days_of_week TEXT,
    recurrence_day_of_month INTEGER,
    recurrence_end_date DATE,
    recurrence_max_occurrences INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

CREATE INDEX idx_task_assignee ON tasks(assignee_id);
CREATE INDEX idx_task_created_by ON tasks(created_by_user_id);
CREATE INDEX idx_task_parent ON tasks(parent_task_id);
CREATE INDEX idx_task_deleted ON tasks(is_deleted);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_due_date ON tasks(due_date);

-- Task-Project multi-homing
CREATE TABLE task_projects (
    task_id UUID NOT NULL REFERENCES tasks(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    PRIMARY KEY (task_id, project_id)
);

-- Task dependencies
CREATE TABLE task_dependencies (
    task_id UUID NOT NULL REFERENCES tasks(id),
    depends_on_task_id UUID NOT NULL REFERENCES tasks(id),
    PRIMARY KEY (task_id, depends_on_task_id)
);

-- Task followers
CREATE TABLE task_followers (
    task_id UUID NOT NULL REFERENCES tasks(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (task_id, user_id)
);

-- Tags table
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7),
    workspace_id UUID NOT NULL REFERENCES workspaces(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- Task tags
CREATE TABLE task_tags (
    task_id UUID NOT NULL REFERENCES tasks(id),
    tag_id UUID NOT NULL REFERENCES tags(id),
    PRIMARY KEY (task_id, tag_id)
);

-- Project tags
CREATE TABLE project_tags (
    project_id UUID NOT NULL REFERENCES projects(id),
    tag_id UUID NOT NULL REFERENCES tags(id),
    PRIMARY KEY (project_id, tag_id)
);

-- Custom fields
CREATE TABLE custom_fields (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    field_key VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_required BOOLEAN DEFAULT FALSE,
    default_value TEXT,
    options TEXT,
    validation_rules TEXT,
    workspace_id UUID NOT NULL REFERENCES workspaces(id),
    display_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- Task custom field values
CREATE TABLE task_custom_field_values (
    task_id UUID NOT NULL REFERENCES tasks(id),
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    field_value TEXT,
    PRIMARY KEY (task_id, custom_field_id)
);

-- Project custom field values
CREATE TABLE project_custom_field_values (
    project_id UUID NOT NULL REFERENCES projects(id),
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id),
    field_value TEXT,
    PRIMARY KEY (project_id, custom_field_id)
);

-- Comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content TEXT NOT NULL,
    task_id UUID NOT NULL REFERENCES tasks(id),
    author_id UUID NOT NULL REFERENCES users(id),
    parent_comment_id UUID REFERENCES comments(id),
    is_edited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- Attachments table
CREATE TABLE attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    s3_key VARCHAR(500),
    task_id UUID REFERENCES tasks(id),
    comment_id UUID REFERENCES comments(id),
    uploaded_by_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- Milestones table
CREATE TABLE milestones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    project_id UUID NOT NULL REFERENCES projects(id),
    status VARCHAR(50) DEFAULT 'UPCOMING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- Audit tables for Hibernate Envers
CREATE TABLE revinfo (
    rev INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    revtstmp BIGINT,
    user_id UUID,
    user_name VARCHAR(255)
);

-- Add audit tables for main entities (example for organizations)
CREATE TABLE organizations_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    name VARCHAR(255),
    display_name VARCHAR(255),
    domain VARCHAR(255),
    description TEXT,
    logo_url VARCHAR(500),
    plan_type VARCHAR(50),
    status VARCHAR(50),
    max_users INTEGER,
    max_projects INTEGER,
    max_storage_gb INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT,
    is_deleted BOOLEAN,
    deleted_at TIMESTAMP,
    deleted_by UUID,
    PRIMARY KEY (id, rev)
);