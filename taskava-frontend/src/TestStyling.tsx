export function TestStyling() {
  return (
    <div className="p-8">
      <h1 className="text-4xl font-bold text-blue-600 mb-4">Tailwind CSS Test</h1>
      <div className="bg-purple-100 p-4 rounded-lg mb-4">
        <p className="text-purple-800">This should have a purple background</p>
      </div>
      <button className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">
        Test Button
      </button>
      <div className="mt-4 grid grid-cols-3 gap-4">
        <div className="bg-red-100 p-4 rounded">Red Box</div>
        <div className="bg-green-100 p-4 rounded">Green Box</div>
        <div className="bg-blue-100 p-4 rounded">Blue Box</div>
      </div>
    </div>
  );
}