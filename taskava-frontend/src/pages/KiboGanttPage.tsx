import React, { useState } from 'react';
import { 
  GanttProvider,
  GanttHeader,
  GanttSidebar,
  GanttSidebarHeader,
  GanttSidebarGroup,
  GanttSidebarItem,
  GanttContentHeader,
  type GanttFeature,
  type GanttStatus
} from '@/components/ui/kibo-ui/gantt';

export function KiboGanttPage() {
  // Sample statuses for Gantt chart
  const statuses: GanttStatus[] = [
    { id: 'planning', name: 'Planning', color: '#3B82F6' },
    { id: 'development', name: 'Development', color: '#F59E0B' },
    { id: 'testing', name: 'Testing', color: '#8B5CF6' },
    { id: 'completed', name: 'Completed', color: '#10B981' },
  ];

  // Sample features for Gantt chart
  const [features] = useState<GanttFeature[]>([
    {
      id: '1',
      name: 'Project Setup',
      startDate: '2024-01-01',
      endDate: '2024-01-07',
      status: statuses[3], // Completed
    },
    {
      id: '2',
      name: 'Backend Development',
      startDate: '2024-01-08',
      endDate: '2024-01-21',
      status: statuses[1], // Development
    },
    {
      id: '3',
      name: 'Frontend Development',
      startDate: '2024-01-15',
      endDate: '2024-01-28',
      status: statuses[1], // Development
    },
    {
      id: '4',
      name: 'Testing & QA',
      startDate: '2024-01-22',
      endDate: '2024-02-04',
      status: statuses[2], // Testing
    },
    {
      id: '5',
      name: 'Deployment',
      startDate: '2024-02-05',
      endDate: '2024-02-07',
      status: statuses[0], // Planning
    },
  ]);

  const handleAddItem = (date: Date) => {
    console.log('Add item at date:', date);
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">Kibo UI Gantt Chart</h1>
        <p className="text-gray-600">
          Visualize project timelines and task dependencies with the Kibo UI Gantt component.
        </p>
      </div>

      <div className="bg-white rounded-lg shadow-sm border">
        <GanttProvider 
          range="monthly" 
          zoom={100}
          onAddItem={handleAddItem}
          className="h-[600px]"
        >
          <div className="flex h-full">
            <GanttSidebar className="w-80">
              <GanttSidebarHeader />
              <GanttSidebarGroup>
                {features.map((feature) => (
                  <GanttSidebarItem
                    key={feature.id}
                    id={feature.id}
                    name={feature.name}
                    status={feature.status}
                  />
                ))}
              </GanttSidebarGroup>
            </GanttSidebar>
            
            <div className="flex-1 overflow-hidden">
              <GanttHeader />
              <GanttContentHeader />
            </div>
          </div>
        </GanttProvider>
      </div>
    </div>
  );
}