-- Create organization_members table for explicit role management
CREATE TABLE IF NOT EXISTS organization_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    invited_by UUID,
    invited_at TIMESTAMP WITH TIME ZONE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by UUID,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_org_member UNIQUE (organization_id, user_id),
    CONSTRAINT ck_org_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'GUEST'))
);

-- Create workspace_members table (replacing the simple join table)
CREATE TABLE IF NOT EXISTS workspace_members_new (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    invited_by UUID,
    invited_at TIMESTAMP WITH TIME ZONE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    can_create_projects BOOLEAN DEFAULT TRUE,
    can_invite_members BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by UUID,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_workspace_member UNIQUE (workspace_id, user_id),
    CONSTRAINT ck_workspace_role CHECK (role IN ('ADMIN', 'MEMBER', 'GUEST'))
);

-- Create team_members table (replacing the simple join table)
CREATE TABLE IF NOT EXISTS team_members_new (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    invited_by UUID,
    invited_at TIMESTAMP WITH TIME ZONE,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by UUID,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_team_member UNIQUE (team_id, user_id),
    CONSTRAINT ck_team_role CHECK (role IN ('LEAD', 'MEMBER', 'GUEST'))
);

-- Migrate existing workspace_members data if exists
INSERT INTO workspace_members_new (workspace_id, user_id, role, joined_at)
SELECT workspace_id, user_id, 'MEMBER', NOW()
FROM workspace_members
ON CONFLICT (workspace_id, user_id) DO NOTHING;

-- Migrate existing team_members data if exists
INSERT INTO team_members_new (team_id, user_id, role, joined_at)
SELECT team_id, user_id, 'MEMBER', NOW()
FROM team_members
ON CONFLICT (team_id, user_id) DO NOTHING;

-- Drop old tables and rename new ones
DROP TABLE IF EXISTS workspace_members CASCADE;
DROP TABLE IF EXISTS team_members CASCADE;

ALTER TABLE workspace_members_new RENAME TO workspace_members;
ALTER TABLE team_members_new RENAME TO team_members;

-- Create indexes
CREATE INDEX idx_org_member_org ON organization_members(organization_id);
CREATE INDEX idx_org_member_user ON organization_members(user_id);
CREATE INDEX idx_org_member_role ON organization_members(role);

CREATE INDEX idx_workspace_member_workspace ON workspace_members(workspace_id);
CREATE INDEX idx_workspace_member_user ON workspace_members(user_id);
CREATE INDEX idx_workspace_member_role ON workspace_members(role);

CREATE INDEX idx_team_member_team ON team_members(team_id);
CREATE INDEX idx_team_member_user ON team_members(user_id);
CREATE INDEX idx_team_member_role ON team_members(role);

-- Add audit trigger functions for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_organization_members_updated_at BEFORE UPDATE ON organization_members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workspace_members_updated_at BEFORE UPDATE ON workspace_members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_team_members_updated_at BEFORE UPDATE ON team_members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();