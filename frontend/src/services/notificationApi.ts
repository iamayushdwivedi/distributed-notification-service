const API_BASE_URL = "http://localhost:8080/api/v1/notifications";

export type Notification = {
  id: number;
  recipient: string;
  message: string;
  channel: string;
  status: string;
  createdAt: string;
};

export type NotificationPage = {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  number: number;
};

export async function fetchNotifications(): Promise<NotificationPage> {
  const response = await fetch(API_BASE_URL);

  if (!response.ok) {
    throw new Error(`Failed to fetch notifications: ${response.status}`);
  }

  return response.json();
}
