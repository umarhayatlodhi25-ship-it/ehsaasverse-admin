import { Settings as SettingsIcon } from 'lucide-react';

const Settings = () => {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold">Settings</h2>
          <p className="text-sm text-gray-500">Configure your admin panel preferences</p>
        </div>
      </div>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center justify-center text-center h-64">
        <SettingsIcon className="w-16 h-16 text-gray-300 mb-4 animate-spin-slow" />
        <h3 className="text-xl font-bold text-gray-600">Settings Module Coming Soon</h3>
        <p className="text-gray-400 mt-2 max-w-md">
          Advanced settings for push notification keys, API limits, and theme customization will be available in future updates.
        </p>
      </div>
    </div>
  );
};

export default Settings;
