import React from 'react';
import { 
  CalendarProvider,
  CalendarHeader,
  CalendarBody,
  CalendarDatePicker,
  CalendarMonthPicker,
  CalendarYearPicker,
  CalendarDatePagination,
  type Feature
} from '@/components/ui/kibo-ui/calendar';

export function KiboCalendarPage() {
  // Sample features for the calendar (Kibo UI calendar expects "features" not "events")
  const calendarFeatures: Feature[] = [
    {
      id: '1',
      name: 'Sprint Planning',
      endDate: '2024-01-15',
      status: { id: 'meeting', name: 'Meeting', color: '#3B82F6' },
    },
    {
      id: '2',
      name: 'Feature Release',
      endDate: '2024-01-20',
      status: { id: 'milestone', name: 'Milestone', color: '#10B981' },
    },
    {
      id: '3',
      name: 'Code Review',
      endDate: '2024-01-18',
      status: { id: 'task', name: 'Task', color: '#F59E0B' },
    },
    {
      id: '4',
      name: 'Team Standup',
      endDate: '2024-01-16',
      status: { id: 'meeting', name: 'Meeting', color: '#3B82F6' },
    },
    {
      id: '5',
      name: 'Demo Day',
      endDate: '2024-01-25',
      status: { id: 'event', name: 'Event', color: '#8B5CF6' },
    },
  ];

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">Kibo UI Calendar</h1>
        <p className="text-gray-600">
          Manage tasks and events with the Kibo UI Calendar component.
        </p>
      </div>

      <div className="bg-white rounded-lg shadow-sm border p-4">
        <CalendarProvider locale="en-US" startDay={0}>
          <div className="space-y-4">
            <CalendarHeader />
            <CalendarBody features={calendarFeatures}>
              {(features) => (
                <div className="space-y-2">
                  {features.map((feature) => (
                    <div
                      key={feature.id}
                      className="p-2 rounded text-sm hover:bg-gray-50 cursor-pointer"
                      style={{ borderLeft: `3px solid ${feature.status.color}` }}
                    >
                      <div className="font-medium">{feature.name}</div>
                      <div className="text-xs text-gray-500">{feature.status.name}</div>
                    </div>
                  ))}
                </div>
              )}
            </CalendarBody>
          </div>
        </CalendarProvider>
      </div>
    </div>
  );
}