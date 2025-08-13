-- Add archived columns to workspaces table
ALTER TABLE workspaces 
ADD COLUMN IF NOT EXISTS is_archived BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS archived_by UUID;

-- Add archived columns to organizations table
ALTER TABLE organizations 
ADD COLUMN IF NOT EXISTS is_archived BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS archived_by UUID;

-- Add archived columns to projects table
ALTER TABLE projects 
ADD COLUMN IF NOT EXISTS is_archived BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS archived_by UUID;

-- Add archived columns to teams table
ALTER TABLE teams 
ADD COLUMN IF NOT EXISTS is_archived BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS archived_by UUID;

-- Add indexes for better query performance on archived columns
CREATE INDEX IF NOT EXISTS idx_workspaces_is_archived ON workspaces(is_archived);
CREATE INDEX IF NOT EXISTS idx_organizations_is_archived ON organizations(is_archived);
CREATE INDEX IF NOT EXISTS idx_projects_is_archived ON projects(is_archived);
CREATE INDEX IF NOT EXISTS idx_teams_is_archived ON teams(is_archived);

-- Add composite indexes for archived_at queries
CREATE INDEX IF NOT EXISTS idx_workspaces_archived_at ON workspaces(archived_at) WHERE archived_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_organizations_archived_at ON organizations(archived_at) WHERE archived_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_projects_archived_at ON projects(archived_at) WHERE archived_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_teams_archived_at ON teams(archived_at) WHERE archived_at IS NOT NULL;