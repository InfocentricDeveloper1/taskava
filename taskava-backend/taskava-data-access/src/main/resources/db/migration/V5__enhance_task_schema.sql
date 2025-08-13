-- V5: Enhance task schema for full multi-homing and Asana-like features
-- This migration adds missing fields and tables for complete task management

-- Add missing columns to tasks table
ALTER TABLE tasks 
    ADD COLUMN IF NOT EXISTS task_type VARCHAR(50) DEFAULT 'task' CHECK (task_type IN ('task', 'milestone', 'approval')),
    ADD COLUMN IF NOT EXISTS due_time TIME,
    ADD COLUMN IF NOT EXISTS likes_count INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS external_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS external_url TEXT;

-- Create indexes for new columns
CREATE INDEX IF NOT EXISTS idx_tasks_task_type ON tasks(task_type) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tasks_external_id ON tasks(external_id) WHERE external_id IS NOT NULL;

-- Drop old task_projects table if exists (it was recreated in V4 with better structure)
-- The V4 migration already has the proper structure, so we just ensure indexes exist
CREATE INDEX IF NOT EXISTS idx_task_projects_added_at ON task_projects(added_at);

-- Task hierarchy table for parent-child relationships (better than single parent_task_id)
CREATE TABLE IF NOT EXISTS task_hierarchy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    child_task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT uk_task_hierarchy UNIQUE (parent_task_id, child_task_id),
    CONSTRAINT chk_no_self_parent CHECK (parent_task_id != child_task_id)
);

-- Indexes for task_hierarchy
CREATE INDEX idx_task_hierarchy_parent ON task_hierarchy(parent_task_id);
CREATE INDEX idx_task_hierarchy_child ON task_hierarchy(child_task_id);
CREATE INDEX idx_task_hierarchy_position ON task_hierarchy(parent_task_id, position);

-- Enhanced task assignees table (supports multiple assignees)
CREATE TABLE IF NOT EXISTS task_assignees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT uk_task_assignee UNIQUE (task_id, user_id)
);

-- Indexes for task_assignees
CREATE INDEX idx_task_assignees_task ON task_assignees(task_id);
CREATE INDEX idx_task_assignees_user ON task_assignees(user_id);

-- Enhanced task dependencies with dependency types
CREATE TABLE IF NOT EXISTS task_dependencies_v2 (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    predecessor_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    successor_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    dependency_type VARCHAR(20) DEFAULT 'finish_start' CHECK (
        dependency_type IN ('finish_start', 'finish_finish', 'start_start', 'start_finish')
    ),
    lag_days INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT uk_task_dependency_v2 UNIQUE (predecessor_id, successor_id),
    CONSTRAINT chk_no_self_dependency CHECK (predecessor_id != successor_id)
);

-- Indexes for task_dependencies_v2
CREATE INDEX idx_task_dependencies_v2_predecessor ON task_dependencies_v2(predecessor_id);
CREATE INDEX idx_task_dependencies_v2_successor ON task_dependencies_v2(successor_id);

-- Task likes (for engagement tracking)
CREATE TABLE IF NOT EXISTS task_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT uk_task_like UNIQUE (task_id, user_id)
);

-- Indexes for task_likes
CREATE INDEX idx_task_likes_task ON task_likes(task_id);
CREATE INDEX idx_task_likes_user ON task_likes(user_id);

-- Task status transitions (for audit and automation)
CREATE TABLE IF NOT EXISTS task_status_transitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    transitioned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    transitioned_by UUID REFERENCES users(id),
    reason TEXT,
    metadata JSONB
);

-- Indexes for task_status_transitions
CREATE INDEX idx_task_transitions_task ON task_status_transitions(task_id);
CREATE INDEX idx_task_transitions_date ON task_status_transitions(transitioned_at);

-- Task custom field values (enhanced structure)
CREATE TABLE IF NOT EXISTS task_custom_field_values_v2 (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    custom_field_id UUID NOT NULL REFERENCES custom_fields(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    
    -- Different value types for different field types
    text_value TEXT,
    number_value DECIMAL(20,6),
    date_value DATE,
    boolean_value BOOLEAN,
    json_value JSONB, -- For complex types like multi-select, people, etc.
    
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT uk_task_custom_value_v2 UNIQUE (task_id, custom_field_id, project_id)
);

-- Indexes for task_custom_field_values_v2
CREATE INDEX idx_task_custom_values_v2_task ON task_custom_field_values_v2(task_id);
CREATE INDEX idx_task_custom_values_v2_field ON task_custom_field_values_v2(custom_field_id);
CREATE INDEX idx_task_custom_values_v2_project ON task_custom_field_values_v2(project_id);

-- Milestone specific data
CREATE TABLE IF NOT EXISTS task_milestones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    target_date DATE NOT NULL,
    completion_percentage INTEGER DEFAULT 0,
    is_key_milestone BOOLEAN DEFAULT FALSE,
    milestone_type VARCHAR(50),
    
    -- Constraints
    CONSTRAINT uk_task_milestone UNIQUE (task_id)
);

-- Approval specific data
CREATE TABLE IF NOT EXISTS task_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    approval_type VARCHAR(50) DEFAULT 'single' CHECK (approval_type IN ('single', 'unanimous', 'majority')),
    required_approvers JSONB, -- Array of user IDs
    approval_status VARCHAR(50) DEFAULT 'pending' CHECK (
        approval_status IN ('pending', 'approved', 'rejected', 'changes_requested')
    ),
    due_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT uk_task_approval UNIQUE (task_id)
);

-- Approval responses
CREATE TABLE IF NOT EXISTS task_approval_responses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_approval_id UUID NOT NULL REFERENCES task_approvals(id) ON DELETE CASCADE,
    approver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    decision VARCHAR(50) NOT NULL CHECK (decision IN ('approved', 'rejected', 'changes_requested')),
    comment TEXT,
    responded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT uk_approval_response UNIQUE (task_approval_id, approver_id)
);

-- Indexes for approval tables
CREATE INDEX idx_task_approvals_task ON task_approvals(task_id);
CREATE INDEX idx_task_approvals_status ON task_approvals(approval_status);
CREATE INDEX idx_approval_responses_approval ON task_approval_responses(task_approval_id);

-- Update priority enum values to match requirements
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_priority_check;
ALTER TABLE tasks ADD CONSTRAINT tasks_priority_check 
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));

-- Update status enum values to match requirements  
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_status_check;
ALTER TABLE tasks ADD CONSTRAINT tasks_status_check 
    CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'BLOCKED', 'COMPLETED', 'CANCELLED'));

-- Add functions for hierarchy validation
CREATE OR REPLACE FUNCTION check_task_hierarchy_cycle()
RETURNS TRIGGER AS $$
DECLARE
    current_parent UUID;
    depth INTEGER := 0;
BEGIN
    -- Check for circular dependencies
    current_parent := NEW.parent_task_id;
    
    WHILE current_parent IS NOT NULL AND depth < 100 LOOP
        IF current_parent = NEW.child_task_id THEN
            RAISE EXCEPTION 'Circular dependency detected in task hierarchy';
        END IF;
        
        SELECT parent_task_id INTO current_parent
        FROM task_hierarchy
        WHERE child_task_id = current_parent
        LIMIT 1;
        
        depth := depth + 1;
    END LOOP;
    
    IF depth >= 100 THEN
        RAISE EXCEPTION 'Task hierarchy depth exceeds maximum allowed (100 levels)';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for hierarchy validation
DROP TRIGGER IF EXISTS validate_task_hierarchy ON task_hierarchy;
CREATE TRIGGER validate_task_hierarchy
    BEFORE INSERT OR UPDATE ON task_hierarchy
    FOR EACH ROW
    EXECUTE FUNCTION check_task_hierarchy_cycle();

-- Add function to update likes count
CREATE OR REPLACE FUNCTION update_task_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE tasks 
        SET likes_count = likes_count + 1 
        WHERE id = NEW.task_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE tasks 
        SET likes_count = GREATEST(likes_count - 1, 0)
        WHERE id = OLD.task_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for likes count
DROP TRIGGER IF EXISTS update_likes_count ON task_likes;
CREATE TRIGGER update_likes_count
    AFTER INSERT OR DELETE ON task_likes
    FOR EACH ROW
    EXECUTE FUNCTION update_task_likes_count();

-- Comments for documentation
COMMENT ON TABLE task_hierarchy IS 'Parent-child relationships between tasks for subtask management';
COMMENT ON TABLE task_assignees IS 'Multiple assignees per task with assignment tracking';
COMMENT ON TABLE task_dependencies_v2 IS 'Task dependencies with different relationship types';
COMMENT ON TABLE task_likes IS 'User likes/reactions on tasks';
COMMENT ON TABLE task_status_transitions IS 'Audit trail of task status changes';
COMMENT ON TABLE task_custom_field_values_v2 IS 'Custom field values for tasks with proper typing';
COMMENT ON TABLE task_milestones IS 'Milestone-specific data for milestone tasks';
COMMENT ON TABLE task_approvals IS 'Approval workflow data for approval tasks';

COMMENT ON COLUMN tasks.task_type IS 'Type of task: task (default), milestone, or approval';
COMMENT ON COLUMN tasks.due_time IS 'Optional time component for due dates';
COMMENT ON COLUMN task_dependencies_v2.dependency_type IS 'Type of dependency: finish_start, finish_finish, start_start, start_finish';
COMMENT ON COLUMN task_dependencies_v2.lag_days IS 'Number of days lag/lead between dependent tasks';