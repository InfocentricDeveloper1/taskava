# Product Requirements Document (PRD) — Asana‑Parity SaaS (Java Spring Boot + React/Shadcn/Tailwind on AWS)

**Version:** 1.2  
**Date:** August 12, 2025  
**Goal:** Build an Asana/ClickUp‑class **multi‑tenant SaaS** with **web (PWA)** + mobile‑friendly UI, on **Java Spring Boot** (API) and **React + Shadcn UI + Tailwind** (frontend), leveraging **AWS** (Cognito, RDS Postgres, S3, CloudFront, SES, ElastiCache/Redis, WAF, GuardDuty, Secrets Manager).  
**Differentiators:** Privacy‑first, affordable pricing, audit/readiness for SOC 1/SOC 2/ISO 27001/GDPR/PDPA/HIPAA‑ready.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [User Personas](#user-personas)
3. [Competitive Differentiation](#competitive-differentiation)
4. [Organizational Hierarchy](#organizational-hierarchy)
5. [Scope Summary (Asana Feature Coverage Map)](#1-scope-summary-asana-feature-coverage-map)
6. [Functional Requirements (Detailed)](#2-functional-requirements-detailed)
7. [Non‑Functional Requirements](#3-non-functional-requirements)
8. [Architecture (AWS)](#4-architecture-aws)
9. [Milestones (3 Months)](#5-milestones-3-months)
10. [Acceptance Criteria](#6-acceptance-criteria-sampling)
11. [Success Metrics & KPIs](#7-success-metrics--kpis)
12. [User Journey Examples](#8-user-journey-examples)
13. [Risks & Notes](#9-risks--notes)
14. [References](#10-references-asana-official-docs--pages)
15. [AI Model Strategy](#ai-model-strategy--gptoss-120b--20b-integration)
16. [Glossary](#glossary)

---

## Executive Summary

This PRD outlines the development of a privacy-focused, enterprise-ready project management platform that achieves feature parity with Asana while offering competitive advantages in data sovereignty, pricing, and compliance readiness. The platform targets mid-market and enterprise teams seeking alternatives to existing solutions due to privacy concerns, compliance requirements, or cost considerations.

---

## User Personas

### 1. **Project Manager (Primary User)**
- **Role**: Coordinates teams, manages projects, tracks progress
- **Pain Points**: Complex pricing tiers, limited data control, compliance concerns
- **Key Needs**: Intuitive project views, automation, reporting, team collaboration
- **Usage**: Daily, 4-6 hours

### 2. **Team Member**
- **Role**: Executes tasks, collaborates on projects
- **Pain Points**: Notification overload, context switching, unclear priorities
- **Key Needs**: Clear task assignments, My Tasks view, minimal friction
- **Usage**: Daily, 2-4 hours

### 3. **Executive/Stakeholder**
- **Role**: Reviews progress, makes decisions, allocates resources
- **Pain Points**: Lack of high-level visibility, scattered reporting
- **Key Needs**: Dashboards, status updates, portfolio views
- **Usage**: Weekly, 30-60 minutes

### 4. **System Administrator**
- **Role**: Manages users, security, compliance, integrations
- **Pain Points**: Limited control over data residency, complex permission management
- **Key Needs**: Granular permissions, audit logs, SSO/SCIM, compliance tools
- **Usage**: As needed, 2-5 hours/week

### 5. **Guest/External Collaborator**
- **Role**: Limited participation in specific projects
- **Pain Points**: Onboarding friction, limited access confusion
- **Key Needs**: Easy access, clear permissions, minimal learning curve
- **Usage**: Varies, project-dependent

### 6. **Scrum Master/Agile Coach** *(New)*
- **Role**: Facilitates agile ceremonies, tracks sprint progress
- **Pain Points**: Manual sprint setup, limited velocity tracking
- **Key Needs**: Sprint management, burndown charts, velocity metrics
- **Usage**: Daily during sprints, 2-3 hours

---

## Competitive Differentiation

### Core Differentiators

1. **Privacy-First Architecture**
   - Self-hosted option for enterprise
   - Data residency controls (choose AWS region)
   - End-to-end encryption option for sensitive projects
   - No third-party analytics or tracking
   - GDPR/PDPA compliant by design

2. **Transparent, Affordable Pricing**
   - No per-user limits on core features
   - Predictable tiered pricing (not per-seat)
   - Free tier includes up to 15 users
   - No feature gating for basic project management
   - Volume discounts for large teams

3. **Compliance-Ready from Day One**
   - Built-in audit logging
   - SOC 2 Type II ready architecture
   - HIPAA-compliant infrastructure option
   - ISO 27001 alignment
   - Export all data anytime

4. **AI-Powered Intelligence**
   - OSS-first AI models (no data leaves your instance)
   - Smart task suggestions
   - Natural language task creation
   - Automated project risk assessment
   - Intelligent resource allocation recommendations

5. **Developer-Friendly**
   - Comprehensive REST API
   - Webhook support
   - CLI tools
   - Open plugin architecture (Post-MVP)
   - Extensive documentation

### Why Choose Us Over Asana/ClickUp?

| Feature | Our Platform | Asana | ClickUp |
|---------|--------------|-------|---------|
| Data Privacy | Self-hosted option, full control | Cloud-only | Cloud-only |
| Pricing Model | Predictable tiers | Per-user, expensive | Complex, changes frequently |
| Compliance | SOC 2/HIPAA ready | Enterprise only | Limited |
| AI Features | OSS models, privacy-first | Limited, cloud-based | Proprietary, cloud-based |
| Export Options | Full data export anytime | Limited | Limited |
| Open Source | Planned roadmap | No | No |

---

## Organizational Hierarchy

### Platform Structure
```
Organization
├── Workspaces (multiple per org)
│   ├── Teams
│   │   ├── Projects
│   │   │   ├── Sections/Lists
│   │   │   │   └── Tasks
│   │   │   │       └── Subtasks
│   │   │   └── Views (List, Board, Calendar, Timeline, Table)
│   │   └── Members
│   └── Portfolios
│       └── Projects (cross-team)
└── Organization Settings
    ├── Admin Console
    ├── Security Settings
    └── Integrations
```

### Permission Levels
- **Organization**: Owner, Admin, Member, Guest
- **Workspace**: Admin, Member, Guest
- **Team**: Manager, Member, Guest
- **Project**: Owner, Editor, Commenter, Viewer

---

## 1) Scope Summary (Asana Feature Coverage Map)
> We enumerate **all major Asana feature areas** and label **MVP** or **Post‑MVP**. MVP is what we ship in 3 months; Post‑MVP includes advanced/enterprise features.

### A. Identity, Org Structure & Permissions
- **Organizations / Workspaces / Teams** (create, join, switch) — **MVP**  
- **Hierarchical structure** (Org → Workspaces → Teams → Projects) — **MVP**
- **Members & Guests (external collaborators)** — **MVP**  
- **Guest limitations** (max 10 per free workspace, domain restrictions) — **MVP**
- **Team management (Overview, All work, Messages, Calendar tabs)** — **MVP**  
- **Project membership & permissions (Editor, Commenter/Viewer, private/public)** — **MVP**  
- **Team permissions & domain‑wide admin controls** — **Post‑MVP**  
- **Admin console (roles, password/MFA policies, SAML/SCIM, IP allowlist, EKM, data residency)** — **Post‑MVP (Enterprise)**  

### B. Projects & Views
- **Projects** (create, archive, delete, owner, members) — **MVP**  
- **Sections/Columns/Lists** — **MVP**  
- **Views: List, Board (Kanban), Calendar, Timeline (basic Gantt)** — **MVP**  
- **Table View** (spreadsheet-like with inline editing) — **MVP**
- **Gantt View (full with critical path)** — **Post‑MVP**  
- **Box View** (workload visualization) — **Post‑MVP**
- **Mind Map View** — **Post‑MVP**
- **Activity View** — **Post‑MVP**
- **Project Overview & Status Updates (on track/at risk/off track/on hold)** — **MVP**  
- **Portfolios (roll‑up of projects) & Portfolio Status** — **MVP** *(moved from Post-MVP)*
- **Saved Views** (personal and shared) — **MVP**

### C. Tasks & Subtasks (Core)
- **Task core**: title, description, assignee(s), followers, due/start date & time, priority, tags — **MVP**  
- **Multiple assignees option** (configurable per organization) — **MVP**
- **Subtasks (nesting, independent tasks, own assignee & fields)** — **MVP**  
- **Important behavior parity**: subtasks **do not auto‑inherit** parent project/assignee — **MVP**  
- **Dependencies ("blocked by" / "blocking")** — **MVP**  
- **Milestones (task type)** — **MVP**  
- **Approvals (approval task type with Approved/Changes/Rejected)** — **MVP**  
- **Custom task types** — **Post‑MVP**
- **Recurring tasks** (repeat rules with end date option) — **MVP**  
- **Multi‑home a task to multiple projects** — **MVP** *(moved from Post-MVP)*
- **Task & Project templates** — **MVP** *(moved from Post-MVP)*
- **Checklists within tasks** — **MVP**

### D. Fields & Customization
- **Custom fields**: text, number, dropdown, date, people, checkbox; per‑project management — **MVP**  
- **Custom statuses** (beyond default To Do/In Progress/Complete) — **MVP**
- **Field library** (organization-wide) — **MVP**
- **Formulas / calculated fields** — **Post‑MVP**  
- **Global fields / field locking** — **Post‑MVP**  
- **Time tracking** (start/stop timer, manual entry) — **MVP** *(moved from Post-MVP)*
- **Time reporting** — **Post‑MVP**

### E. Automation & Workflows
- **Rules (triggers→actions)** — **MVP**  
- **Rule templates library** — **MVP**
- **Automation run log** — **MVP**
- **Maximum 50 rules per project** — **MVP**
- **Conditions & Branching in rules** — **Post‑MVP**  
- **Workflow builder & bundles** — **Post‑MVP**  
- **Rules in My Tasks** — **Post‑MVP**  

### F. Forms (Intake → Tasks)
- **Project forms** (public link, required fields, file upload to task) — **MVP**  
- **Form templates** — **MVP**
- **Forms branching / conditional follow‑ups** — **MVP** *(moved from Post-MVP)*
- **Field mapping to custom fields** — **MVP** *(moved from Post-MVP)*
- **Form analytics** (views, completion rate) — **Post‑MVP**
- **CAPTCHA/spam protection** — **MVP**

### G. Collaboration
- **Comments, @mentions, task followers** — **MVP**  
- **Rich text formatting in comments** — **MVP**
- **File attachments (S3) with preview** — **MVP**
- **Image proofing/annotations** — **Post‑MVP**
- **Messages (team/project)** — **Post‑MVP**  
- **Real-time presence indicators** — **MVP**
- **Live cursors in shared views** — **Post‑MVP**

### H. Personal Productivity
- **My Tasks** with custom sections — **MVP**
- **My Tasks rules** (auto-triage) — **Post‑MVP**
- **Inbox (notification center) + email preferences** — **MVP**  
- **Home dashboard** (personalized overview) — **MVP**
- **Focus mode** — **Post‑MVP**
- **Advanced search & saved searches** — **MVP**
- **Global search with command palette (CMD+K)** — **MVP**

### I. Reporting & Resource Management
- **Project Dashboards (charts, numeric rollups)** — **MVP**  
- **Universal reporting / cross‑project dashboards** — **Post‑MVP**  
- **Portfolios & Portfolio dashboards** — **MVP**
- **Workload view** (basic capacity by assignee) — **MVP** *(moved from Post-MVP)*
- **Advanced workload** (custom capacity, availability) — **Post‑MVP**
- **Time tracking reports** — **Post‑MVP**
- **Custom report builder** — **Post‑MVP**

### J. Strategy & Goals
- **Goals / OKRs (company/team/personal, sub‑goals, alignment)** — **Post‑MVP**  
- **Goal progress tracking** — **Post‑MVP**
- **Goal templates** — **Post‑MVP**

### K. Sprint Management *(New Section)*
- **Sprint creation and management** — **Post‑MVP**
- **Sprint planning tools** — **Post‑MVP**
- **Burndown/burnup charts** — **Post‑MVP**
- **Velocity tracking** — **Post‑MVP**
- **Sprint retrospectives** — **Post‑MVP**

### L. Data In/Out & Integrations
- **CSV import/export (projects/tasks)** — **MVP**  
- **Bulk operations** (edit, move, delete) — **MVP**
- **Calendar sync (Google/Outlook)** — **Post‑MVP**  
- **API + Webhooks** — **MVP**
- **Service Accounts** — **Post‑MVP**
- **Slack integration** — **MVP**
- **Google Drive integration** — **MVP**
- **GitHub/GitLab integration** — **MVP**
- **Email-to-task** — **MVP**

### M. Security & Admin (Enterprise)
- **SSO (SAML), SCIM provisioning** — **Post‑MVP**  
- **Password/MFA policies** — **MVP**
- **Session management** — **MVP**
- **IP allowlisting** — **Post‑MVP**
- **EKM (customer‑managed keys)** — **Post‑MVP**
- **Data residency (US/EU/APAC)** — **Post‑MVP**
- **Audit logs** (90-day retention) — **MVP**
- **Audit log export & SIEM integration** — **Post‑MVP**

---

## 2) Functional Requirements (Detailed)

### 2.1 Organizations, Teams, Members
- Create organization upon first sign‑up; support workspace and team creation within org structure.
- Support multiple workspaces per organization for department isolation.
- Roles: **Organization** (Owner, Admin, Member, Guest); **Workspace** (Admin, Member, Guest); **Team** (Manager, Member, Guest); **Project** (Owner, Editor, Commenter, Viewer).
- Guest limitations: Free tier max 10 guests; domain allowlist/blocklist; limited to assigned projects only.
- Bulk user management: CSV import, bulk role assignment, bulk deactivation.

### 2.2 Projects
- CRUD with **owner**, **members**, **overview tab**, **status updates** (status values, highlights, charts), **archiving**.
- **Views**: List (inline edit), Board (drag columns), Calendar (drag to reschedule), Timeline (drag bars; show dependencies), **Table** (spreadsheet-like).
- View configuration: Show/hide fields, group by any field, sort, filter, color coding.
- **Sections/Columns/Lists**: convert section ↔ column as view changes; custom ordering.
- **Templates**: create from existing project; template gallery with categories; variable placeholders.
- **Project limits**: 10,000 tasks per project; 100 custom fields per project.

### 2.3 Tasks & Subtasks
- **Assignee options**: Single assignee (default) or multiple assignees (org setting).
- Task fields: title, description (rich text with images/links), followers, due/start date+time, priority (High/Medium/Low/None), tags, attachments.
- **Subtasks**: unlimited nesting levels, independent properties, subtask-to-task conversion.
- **Dependencies**: "Blocked by" (predecessors) and "Blocking" (successors), dependency warnings, auto-scheduling option.
- **Task types**: Regular, **Milestone**, **Approval** (Approved/Changes requested/Rejected).
- **Recurring tasks**: Daily/Weekly/Monthly/Yearly; weekday options; end date; regeneration rules.
- **Multi‑homing**: Task appears in multiple projects; changes sync everywhere; project-specific custom fields.
- **Task limits**: 100 subtasks per task; 100 attachments per task; 10MB per attachment (100MB on paid plans).

### 2.4 Customization & Fields
- **Custom fields types**: Text (short/long), Number (integer/decimal), Single-select, Multi-select, Date, People (user selector), Checkbox, Currency, Formula (Post-MVP).
- **Field configuration**: Required/optional, default values, help text, field ordering.
- **Custom statuses**: Create workflow-specific statuses with colors; status groups (To Do/In Progress/Complete categories).
- **Time tracking**: Built-in timer with start/stop; manual time entry; time estimates vs actual; billable flag.
- **Field library**: Organization-wide field definitions; project-level visibility control.

### 2.5 Automation & Rules
- **Triggers**: Task created/updated, status changed, assignee changed, due date approaching (1/3/7 days), custom field changed, form submitted, comment added.
- **Actions**: Move to section, set field value, assign to user, add comment, create subtask, send webhook, add to project, set due date.
- **Rule templates**: Pre-built rules for common workflows (approval flows, task routing, notifications).
- **Automation limits**: 50 rules per project; 1000 rule runs per day per workspace.
- **Run history**: 30-day log with trigger details, actions taken, errors.

### 2.6 Forms (Intake)
- **Form builder**: Drag-drop interface; question types (text, select, file, date, number).
- **Branching logic**: Show/hide questions based on answers; multi-path flows.
- **Form settings**: Public/private, password protection, submission limits, close date.
- **Task creation**: Map form fields to task fields; set default project/section/assignee.
- **Confirmation**: Custom message, redirect URL, email notification.

### 2.7 Collaboration
- **Comments**: Rich text, @mentions (users/teams/projects), attachments, reactions.
- **Attachments**: Upload from device, link from cloud storage, drag-drop; preview for images/PDFs.
- **Activity log**: All changes tracked with user, timestamp, before/after values.
- **Notifications**: In-app, email, mobile push (Phase 2); notification preferences by type.
- **Presence**: See who's viewing same task/project; last seen indicators.

### 2.8 Personal Productivity
- **My Tasks views**: List by project, custom sections, due date grouping, priority sorting.
- **Inbox zero**: Mark all as read, snooze notifications, filter by type.
- **Home dashboard**: Recent tasks, upcoming deadlines, project updates, team activity.
- **Keyboard shortcuts**: Full keyboard navigation; customizable shortcuts.
- **Quick add**: Global task creation (CMD+N); natural language parsing.

### 2.9 Workload & Resource Management
- **Workload view**: See capacity by person; daily/weekly/monthly views.
- **Capacity settings**: Hours per day, task count, or story points; individual capacity overrides.
- **Availability**: Working hours, time off, holidays; capacity auto-adjustment.
- **Workload indicators**: Under capacity (green), at capacity (yellow), over capacity (red).
- **Drag-drop reallocation**: Move tasks between people to balance workload.

### 2.10 Reporting & Analytics
- **Project dashboards**: Task completion, overdue tasks, by assignee, by custom field.
- **Chart types**: Bar, line, pie, donut, number tiles; real-time updates.
- **Dashboard sharing**: Public link, embed code, scheduled email.
- **Data export**: CSV, PDF, PNG for charts.

### 2.11 Data In/Out & API
- **CSV import**: Tasks with all fields, maintain hierarchy, update existing.
- **CSV export**: Current view or all data; scheduled exports.
- **API endpoints**: RESTful; tasks, projects, users, custom fields, comments, attachments.
- **Webhooks**: Project, task, comment events; retry logic; signature verification.
- **Rate limits**: 150 requests/minute standard; 1500 requests/minute enterprise.
- **Migration tools**: Importers for Asana, ClickUp, Trello, Jira.

### 2.12 Security & Admin
- **Authentication**: Email/password, Google, Microsoft, SAML 2.0.
- **MFA**: TOTP, SMS, backup codes; enforcement policies.
- **Password policies**: Minimum length, complexity, rotation, history.
- **Session management**: Timeout settings, concurrent session limits, device management.
- **Audit logging**: User actions, admin actions, data access, API usage.
- **Data protection**: Encryption at rest (AES-256), in transit (TLS 1.2+).
- **Compliance mode**: HIPAA restrictions, audit trail enhancement, PHI indicators.

---

## 3) Non‑Functional Requirements

### Performance
- **API Response**: p95 < 300ms, p99 < 500ms
- **Page Load**: < 2s initial load, < 500ms subsequent navigation
- **Search**: < 200ms for autocomplete, < 1s for full results
- **Real-time updates**: < 100ms latency for same-region users
- **Concurrent users**: Support 1000 concurrent per workspace

### Scalability  
- **Horizontal scaling**: Stateless API servers, read replicas
- **Vertical limits**: 100K tasks per workspace, 10K users per organization
- **Multi-tenancy**: Start with single-DB + RLS, clear migration path to schema-per-tenant
- **Data archival**: Automatic archival of inactive data after 2 years

### Availability & Reliability
- **SLA**: 99.9% uptime (43.8 minutes/month downtime)
- **Disaster Recovery**: RPO < 1 hour, RTO < 4 hours
- **Backups**: Daily automated, 30-day retention, point-in-time recovery
- **Geographic redundancy**: Multi-AZ deployment, optional multi-region

### Security
- **Encryption**: AES-256 at rest, TLS 1.2+ in transit
- **Key management**: AWS KMS with automatic rotation
- **Vulnerability management**: Weekly scans, quarterly penetration tests
- **Incident response**: 24/7 monitoring, 1-hour response time for critical

### Compliance
- **Certifications**: SOC 2 Type II (Year 2), ISO 27001 (Year 2)
- **Regulations**: GDPR, CCPA, PDPA compliant; HIPAA ready
- **Audit trails**: Immutable logs, 7-year retention for compliance
- **Data residency**: Choose from US, EU, APAC regions

### Usability
- **Accessibility**: WCAG 2.1 AA compliant
- **Browser support**: Chrome, Firefox, Safari, Edge (latest 2 versions)
- **Mobile**: Responsive web (MVP), native apps (Phase 2)
- **Localization**: i18n framework; English (MVP), 10 languages (Year 1)

---

## 4) Architecture (AWS)
- **API**: Spring Boot (REST + WebSocket/STOMP), JPA/Hibernate → **RDS Postgres**.  
- **Auth**: **Cognito** Hosted UI → OIDC → app session; PreToken hook for org claims optional.  
- **Cache/Queues**: **ElastiCache Redis** (sessions, pub/sub, rate limits).  
- **Storage**: **S3** for attachments (KMS, presigned URLs); **CloudFront** CDN.  
- **Email**: **SES** (DKIM/SPF/DMARC).  
- **Runtime**: **ECS Fargate** (api, worker, web).  
- **Security**: **WAF**, **GuardDuty**, **CloudTrail**, **AWS Config**.  
- **Observability**: CloudWatch + OpenTelemetry traces.
- **Search**: **OpenSearch** for full-text search and analytics.
- **Real-time**: **API Gateway WebSocket** for live updates.

---

## 5) Milestones (3 Months)

### Month 1
- **Week 1-2**: Architecture setup, CI/CD, development environment
- **Week 3-4**: Auth (Cognito), organization/workspace/team structure
- Core entities: Users, organizations, workspaces, teams
- Basic RBAC implementation
- **Week 5-6**: Projects CRUD, List view, task creation
- **Week 7-8**: CSV import, S3 file attachments, basic API

### Month 2  
- **Week 1-2**: Board, Calendar, Timeline views
- **Week 3-4**: Tasks full implementation (subtasks, dependencies, milestones, approvals)
- **Week 5-6**: Comments, @mentions, activity feed, My Tasks view
- **Week 7-8**: Custom fields, custom statuses, project dashboards

### Month 3
- **Week 1-2**: Automation engine, rule templates, forms with branching
- **Week 3-4**: Multi-homing, portfolios, workload view, search
- **Week 5-6**: Notifications, inbox, integrations (Slack, Google Drive)
- **Week 7-8**: Security hardening, performance optimization, beta launch prep

---

## 6) Acceptance Criteria (Comprehensive)

### Task Management
- Subtask created under a task **does not inherit** parent assignee/project automatically
- Moving subtask to different project maintains link to parent task
- Completing all subtasks does NOT auto-complete parent task
- Task can be assigned to multiple users when org setting enabled
- Deleted task moves to trash for 30 days before permanent deletion

### Permissions
- User with **Comment-only** on project cannot edit tasks, only add comments
- Guest users can only see projects they're explicitly added to
- Private projects invisible to non-members in all searches/reports
- Team member can create projects within their team by default

### Dependencies
- "Blocked by" dependency shows warning banner on blocked task
- Cannot mark task complete while blocking tasks are incomplete (unless override)
- Timeline view shows dependency lines between tasks
- Circular dependencies prevented with validation error

### Custom Fields
- Custom fields addable via project **Customize** menu
- Number fields support decimal places and thousands separators
- Dropdown fields allow color coding of options
- People fields show user avatar and allow multiple selection
- Field changes tracked in activity log

### Automation
- Rules execute within 10 seconds of trigger
- Failed rule shows error in automation log with retry option
- Webhook deliveries retry 3 times with exponential backoff
- Rule loops detected and prevented (max 5 chained rule executions)

### Multi-homing
- Task appears in all projects' views simultaneously
- Project-specific custom fields only show in respective project
- Changing task title updates in all locations within 1 second
- Removing from one project doesn't affect other projects

### Time Tracking
- Timer continues running if user navigates away
- Time entries editable for past 30 days
- Weekly timesheet view exportable to CSV
- Time tracked against subtasks rolls up to parent

---

## 7) Success Metrics & KPIs

### Launch Metrics (First 90 Days)
- **User Acquisition**
  - 1,000 organizations signed up
  - 10,000 total users
  - 25% of signups convert to active projects
  - 15% organic growth through referrals
  
- **User Engagement**
  - 60% WAU (Weekly Active Users)
  - Average 5 tasks created per user per week
  - 80% of users return after first week
  - Average session duration > 15 minutes

- **Feature Adoption**
  - 70% of projects use at least one custom field
  - 50% of projects have active automations
  - 40% of organizations use project dashboards
  - 30% of tasks have dependencies set
  - 25% adoption of time tracking

### Performance Metrics
- **System Performance**
  - 99.9% uptime maintained
  - p95 API response time < 300ms achieved
  - Zero data loss incidents
  - < 0.1% error rate
  - < 2% of requests hit rate limits

### Business Metrics
- **Revenue**
  - 10% conversion to paid plans within 30 days
  - $50K MRR by end of Month 3
  - Average revenue per organization: $150/month
  - Net revenue retention > 100%

- **Customer Satisfaction**
  - NPS > 40
  - Support ticket resolution < 24 hours
  - Feature request implementation cycle < 30 days
  - In-app satisfaction rating > 4.5/5

### Compliance & Security
- **Security Metrics**
  - Zero security breaches
  - 100% of paid organizations using MFA
  - Monthly security audit completion
  - < 5% of login attempts flagged as suspicious

---

## 8) User Journey Examples

### Journey 1: Project Manager Setting Up First Project

**Persona**: Sarah, Marketing Project Manager  
**Goal**: Set up Q4 campaign project and invite team

1. **Sign Up & Onboarding**
   - Creates account with work email
   - Guided tour highlights key features
   - Creates organization "Acme Marketing"
   - Chooses team size and industry for personalized setup

2. **Workspace & Team Creation**
   - Creates "Marketing" workspace
   - Creates "Digital Marketing" team
   - Bulk invites team via CSV upload

3. **Project Creation**
   - Selects "Marketing Campaign" template
   - Customizes sections: Planning, Design, Content, Launch
   - Adds custom fields: Budget (Currency), Channel (Dropdown), Target Audience (Text)
   - Sets project to "Team-visible"

4. **Task Creation**
   - Uses board view to create tasks in each section
   - Sets dependencies: Design brief → Design review → Final assets
   - Assigns tasks with due dates
   - Creates recurring weekly status meeting task

5. **Automation Setup**
   - Creates rule: When task moves to "Launch" → Send Slack notification
   - Sets up form for creative requests → Creates task in "Planning"

6. **Success Moment**
   - Team members join and see personalized My Tasks
   - First status update posted with @mention to VP
   - Dashboard shows project timeline and budget tracking

### Journey 2: Team Member Daily Workflow

**Persona**: Mike, Designer  
**Goal**: Complete daily tasks efficiently

1. **Morning Check-in**
   - Opens My Tasks view grouped by due date
   - Sees 3 tasks due today highlighted in red
   - Reviews new comment notification from PM

2. **Task Execution**
   - Clicks timer to start tracking "Create banner designs"
   - Changes status to custom "In Design" status
   - Uploads 3 design variations as attachments
   - @mentions copywriter for feedback with specific questions

3. **Collaboration**
   - Receives real-time notification of feedback
   - Uses comment thread to discuss changes
   - Updates designs based on feedback
   - Marks subtasks complete as each banner finished

4. **End of Day**
   - Stops timer showing 2.5 hours on task
   - Bulk updates tomorrow's task priorities
   - Sets out-of-office message for Friday
   - Checks workload view to ensure nothing overlooked

### Journey 3: Executive Monthly Review

**Persona**: Lisa, VP of Marketing  
**Goal**: Review team progress and resource allocation

1. **Portfolio Access**
   - Opens "Q4 Marketing Initiatives" portfolio
   - Sees roll-up of 8 active projects with status indicators
   - Identifies 2 at-risk projects (budget overrun, timeline delay)

2. **Deep Dive Analysis**
   - Clicks into at-risk "Product Launch" project
   - Reviews status update explaining supply chain delays
   - Checks workload view showing team at 120% capacity
   - Comments on status update with resource reallocation approval

3. **Resource Optimization**
   - Filters workload by "Design" team
   - Identifies underutilized team member
   - Drags tasks from overloaded designer
   - Adjusts due dates to smooth workload

4. **Strategic Decisions**
   - Reviews portfolio dashboard showing 73% on-track
   - Exports PDF report for board meeting
   - Creates high-priority task for ops manager
   - Approves budget increase via approval task

### Journey 4: External Collaborator Access *(New)*

**Persona**: Alex, Freelance Consultant  
**Goal**: Access project files and submit deliverables

1. **Invitation & Onboarding**
   - Receives email invitation to specific project
   - Creates account with limited profile info
   - Sees only assigned project and tasks

2. **Task Completion**
   - Downloads project brief from task attachment
   - Uploads deliverable with version number
   - Adds comment with completion notes
   - Limited view prevents seeing other projects

3. **Collaboration**
   - Participates in task comments
   - Cannot see team workload or reports
   - Receives only task-specific notifications

---

## 9) Risks & Notes

### Technical Risks
- **Real-time sync complexity**: WebSocket scaling for 10K+ concurrent users requires careful architecture
- **Search performance**: Full-text search across millions of tasks needs dedicated search infrastructure
- **Migration complexity**: Single-DB to multi-tenant migration must be planned from day 1
- **File storage costs**: Large attachments could significantly impact S3 costs

### Business Risks
- **Market saturation**: Asana, ClickUp, Monday.com, Notion competing for same market
- **Feature parity pressure**: Users expect 100% feature match on day 1
- **Price sensitivity**: Free tier might cannibalize paid conversions
- **Enterprise sales cycle**: 6-12 month sales cycles for large organizations

### Mitigation Strategies
- **Phased rollout**: Beta with 100 organizations for feedback
- **Feature flags**: Progressive rollout of complex features
- **Performance budget**: Automated testing to prevent regression
- **Cost controls**: S3 lifecycle policies, CDN caching
- **Competitive intelligence**: Monthly feature comparison updates
- **Enterprise pilot program**: 3-month free trials for enterprise

### MVP Tradeoffs
- Limiting to single assignee initially (multiple assignee as fast-follow)
- Basic workload view (advanced capacity planning in Phase 2)
- English-only (localization framework ready)
- Web-only (mobile responsive but not native)

### Post-MVP Priority Order
1. Advanced workload management
2. Sprint/Agile features
3. Enhanced reporting and analytics
4. Mobile applications
5. Advanced integrations
6. AI-powered features
7. Marketplace/plugin system

---

## 10) References (Asana official docs & pages)
- All Asana features index. 
- Custom fields (types, management). 
- Project permissions & comment‑only. 
- Portfolios & status. 
- Tasks & subtasks behavior. 
- Dependencies. 
- Approvals & milestones. 
- My Tasks & Inbox. 
- Dashboards & reporting. 
- Workload. 
- Goals. 
- CSV import. 
- API & Webhooks. 
- Admin & security catalog (SSO, SCIM, IP allowlist, EKM, Data residency, Sandboxes, etc.). 

**Owner:** <Your Name>  |  **Stakeholders:** Engineering, Design, Security, Compliance, Product, Sales/CS

---

## AI Model Strategy – GPT‑OSS 120B & 20B Integration

### Overview
This SaaS product will adopt an **OSS‑first AI model strategy** to deliver competitive AI capabilities while ensuring data privacy, compliance (SOC 2, HIPAA, GDPR), and cost control.

### Model Selection
- **Default Model (Lightweight tasks):** `gpt‑oss‑20b`
  - Runs on 16 GB GPU instances.
  - Used for lightweight reasoning, natural-language task creation, comment drafting, short summaries.
- **Advanced Reasoning Model:** `gpt‑oss‑120b`
  - Requires ~80 GB GPU (H100/MI300X).
  - Used for deep reasoning, large context summarization, risk prediction, and cross-project insights.
- **Fallback to Commercial LLMs** (optional, per-tenant):
  - OpenAI GPT‑4, Anthropic Claude via AWS Bedrock.
  - Used only if model confidence is low or tenant opts into premium commercial AI.

### Hybrid Routing Logic
1. **OSS-first:** Route all requests to OSS models by default.
2. **Confidence-based fallback:** If OSS model confidence < threshold, route to commercial provider (if allowed).
3. **Per-tenant AI policy:** Tenants can choose "OSS-only", "Hybrid", or "Commercial-only" in settings.

### Infrastructure Plan
- **Serving framework:** vLLM for high-throughput inference.
- **Deployment targets:**
  - `gpt‑oss‑20b` → AWS EC2 G5/G6 instances (16–24 GB VRAM).
  - `gpt‑oss‑120b` → AWS EC2 P5 (H100) or equivalent MI300X instance.
- **Autoscaling:** Scale GPU instances based on request volume.
- **RAG architecture:** pgvector/OpenSearch for embeddings (bge-m3), AI synthesizer for results.
- **Security:** TLS 1.2+, KMS encryption, PII masking before model input.

### Compliance Considerations
- Keep all AI inference within AWS VPC for HIPAA/SOC 2.
- Log all prompts/responses for audit with tenant isolation.
- Maintain red-teaming prompt filters and output sanitizers.

### Roadmap
- **MVP (Phase 1):** OSS-only (20B for standard, 120B for advanced queries).
- **Phase 2:** Hybrid routing with commercial fallback.
- **Phase 3:** Per-tenant fine-tuning and custom model hosting.

---

## Glossary

**Multi-home**: Feature allowing a single task to exist in multiple projects simultaneously, with changes synchronized across all instances.

**Portfolio**: A collection of projects that can be monitored and managed together, typically used for program management or executive oversight.

**Workload**: Visual representation of team capacity showing assigned work against available time.

**Sprint**: Fixed time period (usually 1-4 weeks) for completing a defined set of work in agile methodology.

**Custom Field**: User-defined data fields that can be added to tasks for tracking additional information beyond default fields.

**Rule**: Automated action triggered by specific events or conditions within a project.

**SCIM**: System for Cross-domain Identity Management - protocol for automating user provisioning.

**RLS**: Row-Level Security - database feature ensuring users only access data they're authorized to see.

**Multi-tenant**: Software architecture where single instance serves multiple customers with data isolation.

**Webhook**: HTTP callback that sends real-time information to other applications when events occur.

---

# Asana & ClickUp Feature Parity PRD (Continued)

## Platform Details

**Platform:** Java Spring Boot (Backend) + React + Shadcn + Tailwind (Frontend)  
**Target Platforms:** Web (MVP), Mobile (Phase 2)  
**MVP Timeline:** 3 months  
**Differentiators:** Privacy-first, Competitive Pricing, Compliance (SOC 1, SOC 2, HIPAA, GDPR)

## 1. Vision
Build a SaaS project management platform replicating and enhancing Asana and ClickUp core functionality, optimized for AWS infrastructure, designed for security, scalability, and compliance.

## 2. Core Technology Stack
- **Backend:** Java Spring Boot, JPA/Hibernate, REST API, JWT auth
- **Frontend:** React + TypeScript, Shadcn UI components, TailwindCSS
- **Database:** AWS RDS (PostgreSQL)
- **Authentication:** AWS Cognito (OIDC, SAML, MFA, Social logins)
- **Storage:** AWS S3 (file attachments, per-tenant prefixes)
- **Monitoring:** AWS CloudWatch, AWS X-Ray
- **Email/Notifications:** AWS SES + SNS
- **Hosting:** AWS ECS/Fargate or EKS, CloudFront CDN

## 3. Multi-Tenancy & Security
- **Pattern:** Single DB with tenant_id + Row-Level Security (RLS)
- Optional: Schema-per-tenant (Enterprise tier)
- **Isolation:** Per-tenant S3 prefix, per-tenant encryption context in AWS KMS
- **Access Control:** RBAC with tenant-level and project-level permissions
- **Compliance Readiness:** SOC 1, SOC 2, HIPAA, GDPR, PDPA

## 4. Feature Set (MVP) - Enhanced

### 4.1 Project & Task Management
- Create/edit/delete projects with templates
- Create/edit/delete tasks & subtasks (unlimited nesting)
- Single or multiple assignees (configurable)
- Custom fields (text, number, date, dropdown, people, checkbox)
- Task dependencies with visualization
- Due dates, start dates with time
- Task priorities (4 levels + none)
- Bulk actions (up to 200 items)
- Recurring tasks with flexible patterns
- Task types (regular, milestone, approval)
- Multi-homing across projects
- Checklists within tasks

### 4.2 Views & Filtering
- List view with inline editing
- Kanban board with WIP limits
- Calendar view with drag-drop
- Timeline/Gantt with dependencies
- Table view (spreadsheet-like)
- Saved views (personal/shared)
- Advanced filtering with AND/OR logic
- Grouping by any field
- Sorting (multi-level)

### 4.3 Collaboration & Communication
- @mentions with autocomplete
- Threaded comments
- Rich text formatting
- Task activity log
- File attachments with preview
- Real-time presence indicators
- Read receipts for comments
- Emoji reactions

### 4.4 User & Team Management
- Bulk invite via CSV
- Roles: Owner, Admin, Member, Guest
- Team hierarchy support
- Guest limitations and controls
- User profiles with skills/timezone
- Team calendar

### 4.5 Notifications & Reminders
- Intelligent notification batching
- Email digest options
- In-app notification center
- Browser push notifications
- Mobile push (Phase 2)
- Notification preferences by type
- Do not disturb modes

### 4.6 Search & Reporting
- Global search with filters
- Command palette (CMD+K)
- Natural language search
- Saved searches
- Search history
- Basic dashboards (10 widgets)
- Export to CSV/PDF

### 4.7 Time & Resource Management
- Built-in time tracking
- Timer with idle detection
- Manual time entries
- Time estimates
- Basic workload view
- Capacity settings

### 4.8 Automation & Productivity
- Rule templates (20+)
- Custom automation rules
- Form builder with logic
- Email-to-task
- Keyboard shortcuts
- Quick task creation
- Bulk operations

## 5. AWS Service Integrations

| AWS Service | Purpose |
|-------------|---------|
| Cognito | Auth, MFA, social logins |
| RDS PostgreSQL | Main DB with RLS |
| S3 | File storage with lifecycle |
| CloudWatch | Logs & metrics |
| SES | Email notifications |
| SNS | Push notifications |
| KMS | Encryption keys |
| CloudFront | CDN for assets |
| WAF | Security rules |
| OpenSearch | Full-text search |
| ElastiCache | Redis caching |
| API Gateway | WebSocket support |

## 6. Compliance Considerations
- **SOC 1 / SOC 2:** Continuous monitoring, quarterly reviews
- **HIPAA:** BAA with AWS, access controls, audit logs
- **GDPR / PDPA:** Data portability, right to deletion, consent management
- **Security:** OWASP Top 10 protection, dependency scanning

## 7. Mobile (Phase 2)
- React Native for code reuse
- Offline-first architecture
- Background sync
- Biometric authentication
- Push notifications
- Camera integration

## 8. Deployment Plan
- **Environments:** Dev, Stage, Prod, DR
- **CI/CD:** GitHub Actions with security scanning
- **IaC:** Terraform with state management
- **Database:** Flyway migrations with rollback
- **Monitoring:** APM with Datadog/New Relic
- **Feature flags:** LaunchDarkly integration

## 9. Success Metrics
- User activation rate > 80%
- Task creation within first hour > 90%
- Daily active users > 40%
- Workspace retention (90 days) > 85%
- Time to first value < 10 minutes
- Support ticket rate < 5%

## 10. Future Enhancements (Post-MVP)
- AI task prioritization and suggestions
- Advanced analytics with ML insights
- Voice commands and transcription
- AR/VR meeting integration
- Blockchain for audit trails
- Marketplace with revenue sharing
- White-label options

---

## Critical Additions for True Feature Parity

### Multi-home Tasks (MVP)
- Task maintains single source of truth
- Appears in all project views simultaneously  
- Project-specific context (section, custom fields)
- Breadcrumb shows all project locations
- Quick switcher between project contexts

### Portfolios (MVP)
- Roll-up view of multiple projects
- Progress aggregation (% complete, tasks)
- Status inheritance and override
- Portfolio-level custom fields
- Nested portfolios (Post-MVP)
- Executive dashboards

### Forms (MVP)
- Conditional logic with multiple branches
- Field validation rules
- File upload support (multiple files)
- Progress indicator
- Save and resume capability
- Form analytics dashboard
- Embed options (iframe, JavaScript)

### Automations Enhanced
- Visual workflow builder
- Test mode with simulation
- Approval workflows
- Escalation rules
- SLA monitoring
- Cross-project automation
- External triggers via webhooks

### Approvals Workflow
- Sequential and parallel approval paths
- Delegate approval capability
- Approval history and audit trail
- Email approval (reply to approve)
- Bulk approvals
- Conditional approval routing

### Proofing & Annotation
- Version comparison
- Threaded discussions on specific areas
- Drawing tools (arrow, box, highlight)
- Video timestamp comments
- Resolution tracking
- Approval on proofs

### Advanced View Features
- View templates library
- Formula columns in table view
- Conditional formatting
- Cross-project views
- Embedded views (public/private)
- View analytics

### HIPAA Compliance Mode
- PHI field marking
- Enhanced audit logging
- Automatic data retention policies
- Encrypted attachments
- Access reports
- Break-glass access

### Enterprise Key Management (EKM)
- Customer-managed keys in AWS KMS
- Key rotation policies
- Per-workspace encryption
- Crypto-shredding capability
- Key usage audit logs

### Integration Specifications

**Slack Integration (MVP)**
- Two-way sync for comments
- Task creation from messages
- Status updates in channels
- Slash commands
- Unfurling task links

**Google Workspace (MVP)**
- Drive file picker
- Docs collaborative editing
- Calendar two-way sync
- Gmail add-on
- Meet integration

**Developer Tools (MVP)**
- GitHub pull request linking
- Commit message parsing
- CI/CD status in tasks
- Code review assignments
- Branch protection rules

### Global Limits & Quotas

**Free Tier**
- 2 active projects
- 15 users
- 100MB file storage
- 10 automation rules
- 30-day activity history

**Paid Tiers**
- Unlimited projects
- 10GB file storage per user
- 100 automation rules per project
- 2-year activity history
- Priority support

**Rate Limits**
- API: 150 req/min (standard), 1500 req/min (enterprise)
- Webhooks: 10K events/hour
- Bulk operations: 500 items
- File uploads: 100MB (standard), 1GB (enterprise)

---

This enhanced PRD now provides comprehensive feature parity with both Asana and ClickUp while maintaining focus on your core differentiators. The document includes all necessary business requirements and feature specifications needed for the development team to build a competitive project management platform.

---
**Related Documents:** [TASKS.md](TASKS.md)
