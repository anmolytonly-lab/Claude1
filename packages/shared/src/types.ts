// Core domain types shared across Nova's apps (mobile now, web/server later).
// These mirror the eventual Supabase table shapes so the mock data layer
// can be swapped for real Supabase/server calls without changing call sites.

export type ThemePreference = "light" | "dark" | "system";

export interface Profile {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  themePreference: ThemePreference;
  activeOrganizationId: string;
  createdAt: string;
}

export interface Organization {
  id: string;
  name: string;
  isPersonal: boolean;
  role: "owner" | "admin" | "member";
  createdAt: string;
}

export type MessageRole = "user" | "assistant" | "system";

export interface Message {
  id: string;
  conversationId: string;
  role: MessageRole;
  content: string;
  createdAt: string;
  pending?: boolean;
}

export interface Conversation {
  id: string;
  title: string;
  pinned: boolean;
  folder?: string;
  createdAt: string;
  updatedAt: string;
  lastMessagePreview?: string;
}

export interface MemoryItem {
  id: string;
  summary: string;
  sourceConversationId?: string;
  createdAt: string;
}

export type TaskStatus = "todo" | "in_progress" | "done";
export type TaskPriority = "low" | "medium" | "high";

export interface Task {
  id: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Note {
  id: string;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface CalendarEvent {
  id: string;
  title: string;
  description?: string;
  startsAt: string;
  endsAt: string;
  location?: string;
  createdAt: string;
}

export type AgentRunStatus = "planning" | "running" | "waiting_approval" | "completed" | "failed" | "cancelled";

export interface AgentStep {
  id: string;
  summary: string;
  toolName?: string;
  status: "pending" | "running" | "success" | "error" | "awaiting_approval";
  output?: string;
}

export interface AgentRun {
  id: string;
  goal: string;
  status: AgentRunStatus;
  plan: string[];
  steps: AgentStep[];
  finalResult?: string;
  createdAt: string;
}
