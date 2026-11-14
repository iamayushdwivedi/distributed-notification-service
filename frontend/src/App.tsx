import {
  Activity,
  Bell,
  CheckCircle2,
  Clock3,
  LayoutDashboard,
  Settings,
  XCircle,
} from "lucide-react";

import { useEffect, useState } from "react";
import { Plus } from "lucide-react";
import {
  fetchNotifications,
  createNotification,
  type Notification,
} from "./services/notificationApi";

function App() {
  const [activeTab, setActiveTab] = useState<
    "overview" | "notifications" | "analytics" | "settings"
  >("overview");
  const [showForm, setShowForm] = useState(false);
  const [recipient, setRecipient] = useState("");
  const [channel, setChannel] = useState("EMAIL");
  const [message, setMessage] = useState("");
  const [formSubmitted, setFormSubmitted] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalNotifications, setTotalNotifications] = useState(0);

  useEffect(() => {
    async function loadNotifications() {
      try {
        const data = await fetchNotifications();
        setNotifications(data.content);
        setTotalNotifications(data.totalElements);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Something went wrong");
      } finally {
        setLoading(false);
      }
    }

    loadNotifications();
  }, []);
  const sentCount = notifications.filter(
    (notification) => notification.status === "SENT",
  ).length;

  const pendingCount = notifications.filter(
    (notification) => notification.status === "PENDING",
  ).length;

  const failedCount = notifications.filter(
    (notification) => notification.status === "FAILED",
  ).length;
  return (
    <div className="flex min-h-screen bg-slate-950 text-white">
      {/* Sidebar */}
      <aside className="flex w-64 flex-col border-r border-slate-800 bg-slate-900 p-6">
        {/* Branding */}
        <div className="mb-12 flex items-center gap-3">
          <div className="rounded-xl bg-emerald-500 p-2">
            <Bell className="h-6 w-6 text-slate-950" />
          </div>

          <h1 className="text-2xl font-bold">NotifyFlow</h1>
        </div>

        {/* Navigation */}
        <nav className="space-y-2">
          <button
            type="button"
            onClick={() => setActiveTab("overview")}
            className={`flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition ${
              activeTab === "overview"
                ? "bg-emerald-500/10 text-emerald-400"
                : "text-slate-400 hover:bg-slate-800 hover:text-white"
            }`}
          >
            <LayoutDashboard size={20} />
            Overview
          </button>

          <button
            type="button"
            onClick={() => setActiveTab("notifications")}
            className={`flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition ${
              activeTab === "notifications"
                ? "bg-emerald-500/10 text-emerald-400"
                : "text-slate-400 hover:bg-slate-800 hover:text-white"
            }`}
          >
            <Bell size={20} />
            Notifications
          </button>

          <button
            type="button"
            onClick={() => setActiveTab("analytics")}
            className={`flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition ${
              activeTab === "analytics"
                ? "bg-emerald-500/10 text-emerald-400"
                : "text-slate-400 hover:bg-slate-800 hover:text-white"
            }`}
          >
            <Activity size={20} />
            Analytics
          </button>

          <button
            type="button"
            onClick={() => setActiveTab("settings")}
            className={`flex w-full items-center gap-3 rounded-lg px-4 py-3 text-left transition ${
              activeTab === "settings"
                ? "bg-emerald-500/10 text-emerald-400"
                : "text-slate-400 hover:bg-slate-800 hover:text-white"
            }`}
          >
            <Settings size={20} />
            Settings
          </button>
        </nav>

        {/* Sidebar Footer */}
        <div className="mt-auto border-t border-slate-800 pt-6">
          <p className="text-xs text-slate-500">NotifyFlow v1.0</p>
        </div>
      </aside>

      {/* Main Content */}

      {/* Main Content */}
      <main id="overview" className="flex-1 p-10">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <h2 className="text-3xl font-bold">
            {activeTab === "overview"
              ? "Dashboard Overview"
              : activeTab === "notifications"
                ? "Notifications"
                : activeTab === "analytics"
                  ? "Analytics"
                  : "Settings"}
          </h2>

          {(activeTab === "overview" || activeTab === "notifications") && (
            <button
              type="button"
              onClick={() => {
                setShowForm(!showForm);
                setFormSubmitted(false);
              }}
              className="flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2.5 font-medium text-slate-950 transition hover:bg-emerald-400"
            >
              <Plus size={18} />
              {showForm ? "Cancel" : "New Notification"}
            </button>
          )}
        </div>

        <p className="mt-2 text-slate-400">
          Monitor and manage your distributed notifications.
        </p>

        {showForm &&
          (activeTab === "overview" || activeTab === "notifications") && (
            <section className="mt-6 rounded-xl border border-slate-800 bg-slate-900 p-6">
              <h3 className="text-lg font-semibold">Create Notification</h3>
              {formSubmitted &&
                (activeTab === "overview" || activeTab === "notifications") && (
                  <div
                    role="status"
                    className="mt-4 rounded-lg border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-400"
                  >
                    Notification request submitted successfully!
                  </div>
                )}
              <form
                className="mt-6 space-y-5"
                onSubmit={async (event) => {
                  event.preventDefault();

                  try {
                    await createNotification({
                      recipient,
                      channel,
                      message,
                    });

                    const updatedData = await fetchNotifications();

                    setNotifications(updatedData.content);
                    setTotalNotifications(updatedData.totalElements);

                    setFormSubmitted(true);
                  } catch (err) {
                    console.error("Failed to create notification:", err);
                    alert("Failed to create notification. Check the console.");
                  }
                }}
              >
                {/* Recipient */}
                <div>
                  <label
                    htmlFor="recipient"
                    className="mb-2 block text-sm font-medium text-slate-300"
                  >
                    Recipient Email
                  </label>

                  <input
                    id="recipient"
                    type="email"
                    required
                    value={recipient}
                    onChange={(event) => setRecipient(event.target.value)}
                    placeholder="user@example.com"
                    className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-emerald-500"
                  />
                </div>

                {/* Channel */}
                <div>
                  <label
                    htmlFor="channel"
                    className="mb-2 block text-sm font-medium text-slate-300"
                  >
                    Channel
                  </label>

                  <select
                    id="channel"
                    value={channel}
                    onChange={(event) => setChannel(event.target.value)}
                    className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-emerald-500"
                  >
                    <option value="EMAIL">Email</option>
                  </select>
                </div>

                {/* Message */}
                <div>
                  <label
                    htmlFor="message"
                    className="mb-2 block text-sm font-medium text-slate-300"
                  >
                    Message
                  </label>

                  <textarea
                    id="message"
                    required
                    rows={4}
                    value={message}
                    onChange={(event) => setMessage(event.target.value)}
                    placeholder="Enter your notification message..."
                    className="w-full resize-none rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none focus:border-emerald-500"
                  />
                </div>

                <button
                  type="submit"
                  className="rounded-lg bg-emerald-500 px-6 py-3 font-semibold text-slate-950 transition hover:bg-emerald-400"
                >
                  Create Notification
                </button>
              </form>
            </section>
          )}

        {/* Statistics Cards */}
        {(activeTab === "overview" || activeTab === "notifications") && (
          <>
            {activeTab === "overview" && (
              <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
                <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-slate-400">
                      Total Notifications
                    </p>
                    <Bell className="h-5 w-5 text-blue-400" />
                  </div>
                  <h3 className="mt-4 text-3xl font-bold">
                    {totalNotifications}
                  </h3>
                </div>

                <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-slate-400">Sent</p>
                    <CheckCircle2 className="h-5 w-5 text-emerald-400" />
                  </div>
                  <h3 className="mt-4 text-3xl font-bold">{sentCount}</h3>
                </div>

                <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-slate-400">Pending</p>
                    <Clock3 className="h-5 w-5 text-amber-400" />
                  </div>
                  <h3 className="mt-4 text-3xl font-bold">{pendingCount}</h3>
                </div>

                <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-slate-400">Failed</p>
                    <XCircle className="h-5 w-5 text-rose-400" />
                  </div>
                  <h3 className="mt-4 text-3xl font-bold">{failedCount}</h3>
                </div>
              </div>
            )}

            {/* Recent Notifications */}
            {loading && (
              <p className="mt-4 text-slate-400">Loading notifications...</p>
            )}

            {error && (
              <p role="alert" className="mt-4 text-rose-400">
                Error: {error}
              </p>
            )}
            <section
              id="notifications"
              className="mt-8 overflow-hidden rounded-xl border border-slate-800 bg-slate-900"
            >
              <div className="border-b border-slate-800 px-6 py-5">
                <h3 className="text-lg font-semibold">Recent Notifications</h3>
                <p className="mt-1 text-sm text-slate-400">
                  Latest notification activity
                </p>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-800/50 text-slate-400">
                    <tr>
                      <th className="px-6 py-4 font-medium">ID</th>
                      <th className="px-6 py-4 font-medium">Recipient</th>
                      <th className="px-6 py-4 font-medium">Channel</th>
                      <th className="px-6 py-4 font-medium">Status</th>
                    </tr>
                  </thead>

                  <tbody className="divide-y divide-slate-800">
                    {notifications.map((notification) => (
                      <tr
                        key={notification.id}
                        className="hover:bg-slate-800/40"
                      >
                        <td className="px-6 py-4 text-slate-400">
                          #{notification.id}
                        </td>

                        <td className="px-6 py-4">{notification.recipient}</td>

                        <td className="px-6 py-4 text-slate-300">
                          {notification.channel}
                        </td>

                        <td className="px-6 py-4">
                          <span
                            className={`rounded-full px-3 py-1 text-xs font-medium ${
                              notification.status === "SENT"
                                ? "bg-emerald-500/10 text-emerald-400"
                                : notification.status === "PENDING"
                                  ? "bg-amber-500/10 text-amber-400"
                                  : "bg-rose-500/10 text-rose-400"
                            }`}
                          >
                            {notification.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          </>
        )}
        {activeTab === "analytics" && (
          <section className="mt-8 rounded-xl border border-slate-800 bg-slate-900 p-6">
            <h3 className="text-xl font-semibold">Notification Analytics</h3>

            <p className="mt-2 text-sm text-slate-400">
              Status breakdown for the {notifications.length} most recently
              loaded notifications.
            </p>

            <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
              <div className="rounded-lg bg-slate-800 p-5">
                <p className="text-sm text-slate-400">Sent</p>
                <p className="mt-2 text-3xl font-bold text-emerald-400">
                  {sentCount}
                </p>
              </div>

              <div className="rounded-lg bg-slate-800 p-5">
                <p className="text-sm text-slate-400">Pending</p>
                <p className="mt-2 text-3xl font-bold text-amber-400">
                  {pendingCount}
                </p>
              </div>

              <div className="rounded-lg bg-slate-800 p-5">
                <p className="text-sm text-slate-400">Failed</p>
                <p className="mt-2 text-3xl font-bold text-rose-400">
                  {failedCount}
                </p>
              </div>
            </div>
          </section>
        )}
        {activeTab === "settings" && (
          <section className="mt-8 rounded-xl border border-slate-800 bg-slate-900 p-6">
            <h3 className="text-xl font-semibold">Application Information</h3>

            <p className="mt-2 text-sm text-slate-400">
              Current NotifyFlow configuration and supported features.
            </p>

            <div className="mt-6 divide-y divide-slate-800">
              <div className="flex justify-between py-4">
                <span className="text-slate-400">Application</span>
                <span>NotifyFlow</span>
              </div>

              <div className="flex justify-between py-4">
                <span className="text-slate-400">Version</span>
                <span>1.0</span>
              </div>

              <div className="flex justify-between py-4">
                <span className="text-slate-400">Notification Channel</span>
                <span>Email</span>
              </div>

              <div className="flex justify-between py-4">
                <span className="text-slate-400">Message Broker</span>
                <span>Apache Kafka</span>
              </div>

              <div className="flex justify-between py-4">
                <span className="text-slate-400">Database</span>
                <span>PostgreSQL</span>
              </div>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}

export default App;
