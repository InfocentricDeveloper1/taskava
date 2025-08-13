-- V4: Create project and related tables for multi-homing architecture
-- Note: Complex constraints are enforced at the application level

-- Projects table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    team_id UUID REFERENCES teams(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#1e90ff',
    icon VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED', 'COMPLETED')),
    status_update JSONB,
    privacy VARCHAR(50) NOT NULL DEFAULT 'TEAM_VISIBLE' CHECK (privacy IN ('TEAM_VISIBLE', 'WORKSPACE_VISIBLE', 'PUBLIC')),
    settings JSONB DEFAULT '{}',
    archived_at TIMESTAMPTZ,
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID REFERENCES users(id)
);

-- Indexes for projects
CREATE INDEX idx_projects_workspace ON projects(workspace_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_projects_team ON projects(team_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_projects_status ON projects(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_projects_deleted ON projects(is_deleted);
CREATE INDEX idx_projects_archived ON projects(archived_at) WHERE archived_at IS NOT NULL;
-- Unique constraint on workspace + name for active projects
CREATE UNIQUE INDEX uk_project_workspace_name ON projects(workspace_id, name) WHERE is_deleted = FALSE;

-- Project sections (kanban columns)
CREATE TABLE IF NOT EXISTS project_sections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID REFERENCES users(id)
);

-- Indexes for project_sections
CREATE INDEX idx_section_project ON project_sections(project_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_section_position ON project_sections(position) WHERE is_deleted = FALSE;
-- Unique constraint on project + position for active sections
CREATE UNIQUE INDEX uk_project_section_position ON project_sections(project_id, position) WHERE is_deleted = FALSE;

-- Task-Project relationship (multi-homing support)
CREATE TABLE IF NOT EXISTS task_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    section_id UUID REFERENCES project_sections(id) ON DELETE SET NULL,
    position INTEGER NOT NULL DEFAULT 0,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    added_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT uk_task_project UNIQUE (task_id, project_id)
);

-- Indexes for task_projects
CREATE INDEX idx_task_projects_task ON task_projects(task_id);
CREATE INDEX idx_task_projects_project ON task_projects(project_id);
CREATE INDEX idx_task_projects_position ON task_projects(project_id, position);
CREATE INDEX idx_task_projects_section ON task_projects(section_id);

-- Project members
CREATE TABLE IF NOT EXISTS project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'MEMBER',
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    added_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT uk_project_member UNIQUE (project_id, user_id)
);

-- Indexes for project_members
CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);

-- Forms table (stub for future implementation)
CREATE TABLE IF NOT EXISTS forms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    fields JSONB NOT NULL,
    settings JSONB DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    submission_count INTEGER DEFAULT 0,
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID REFERENCES users(id)
);

-- Indexes for forms
CREATE INDEX idx_form_project ON forms(project_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_form_active ON forms(is_active) WHERE is_deleted = FALSE;

-- Automation rules table (stub for future implementation)
CREATE TABLE IF NOT EXISTS automation_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    trigger_type VARCHAR(100) NOT NULL,
    trigger_config JSONB DEFAULT '{}',
    conditions JSONB DEFAULT '[]',
    actions JSONB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    execution_count INTEGER DEFAULT 0,
    
    -- Audit fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID REFERENCES users(id)
);

-- Indexes for automation_rules
CREATE INDEX idx_automation_project ON automation_rules(project_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_automation_active ON automation_rules(is_active) WHERE is_deleted = FALSE;

-- Project custom fields (many-to-many)
CREATE TABLE IF NOT EXISTS project_custom_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id) ON DELETE CASCADE,
    is_required BOOLEAN DEFAULT FALSE,
    position INTEGER NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT uk_project_custom_field UNIQUE (project_id, custom_field_id)
);

-- Indexes for project_custom_fields
CREATE INDEX idx_project_custom_fields_project ON project_custom_fields(project_id);
CREATE INDEX idx_project_custom_fields_field ON project_custom_fields(custom_field_id);

-- Comments for documentation
COMMENT ON TABLE projects IS 'Projects within workspaces that contain tasks';
COMMENT ON TABLE project_sections IS 'Sections/columns within projects for organizing tasks';
COMMENT ON TABLE task_projects IS 'Multi-homing relationship between tasks and projects';
COMMENT ON TABLE project_members IS 'Users who are members of specific projects';
COMMENT ON TABLE forms IS 'Forms for collecting information and creating tasks';
COMMENT ON TABLE automation_rules IS 'Automation rules for projects';

COMMENT ON COLUMN projects.status IS 'Project status: ACTIVE, ARCHIVED, or COMPLETED';
COMMENT ON COLUMN projects.privacy IS 'Project visibility: TEAM_VISIBLE, WORKSPACE_VISIBLE, or PUBLIC';
COMMENT ON COLUMN projects.status_update IS 'JSON object containing latest status update information';
COMMENT ON COLUMN projects.settings IS 'JSON object containing project-specific settings';
COMMENT ON COLUMN task_projects.position IS 'Position of task within the project/section for ordering';