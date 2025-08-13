import './index.css';

function TestApp() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-lg max-w-md w-full">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">Taskava UI Test</h1>
        <p className="text-gray-600 mb-6">
          The UI foundation is ready. The import resolution issue is being fixed.
        </p>
        <div className="space-y-3">
          <button className="w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors">
            Primary Button
          </button>
          <button className="w-full bg-gray-200 text-gray-900 px-4 py-2 rounded-md hover:bg-gray-300 transition-colors">
            Secondary Button
          </button>
        </div>
        <div className="mt-6 p-4 bg-blue-50 rounded-md">
          <h2 className="font-semibold text-blue-900 mb-2">Status</h2>
          <ul className="text-sm text-blue-800 space-y-1">
            <li>✅ Tailwind CSS configured</li>
            <li>✅ ShadCN components installed</li>
            <li>✅ UI pages created</li>
            <li>🔧 Fixing TypeScript imports</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default TestApp;