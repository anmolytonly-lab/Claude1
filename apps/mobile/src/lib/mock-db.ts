import type {
  CalendarEvent,
  Conversation,
  MemoryItem,
  Message,
  Note,
  Organization,
  Profile,
  Task,
  TaskStatus,
} from '@nova/shared';

import { generateId } from '@/lib/id';
import { readJSON, writeJSON } from '@/lib/storage';

// A fully local, in-memory + AsyncStorage-persisted stand-in for the real
// Supabase/server backend. Every function mirrors the shape a future real
// API call would have (async, returns plain data) so the UI layer doesn't
// need to change when this is swapped for live calls.

interface DbShape {
  profile: Profile;
  organizations: Organization[];
  conversations: Conversation[];
  messagesByConversation: Record<string, Message[]>;
  memories: MemoryItem[];
  tasks: Task[];
  notes: Note[];
  calendarEvents: CalendarEvent[];
}

const DB_KEY = 'db';

function now(): string {
  return new Date().toISOString();
}

function seedDb(): DbShape {
  const orgId = generateId('org');
  const conversationId = generateId('conv');

  return {
    profile: {
      id: generateId('user'),
      displayName: 'You',
      themePreference: 'system',
      activeOrganizationId: orgId,
      createdAt: now(),
    },
    organizations: [
      { id: orgId, name: 'Personal', isPersonal: true, role: 'owner', createdAt: now() },
    ],
    conversations: [
      {
        id: conversationId,
        title: 'Welcome to Nova',
        pinned: true,
        createdAt: now(),
        updatedAt: now(),
        lastMessagePreview: "Hi! I'm Nova. Ask me anything to get started.",
      },
    ],
    messagesByConversation: {
      [conversationId]: [
        {
          id: generateId('msg'),
          conversationId,
          role: 'assistant',
          content: "Hi! I'm Nova, your personal AI assistant. Ask me anything to get started.",
          createdAt: now(),
        },
      ],
    },
    memories: [
      {
        id: generateId('mem'),
        summary: 'User is setting up Nova for the first time.',
        createdAt: now(),
      },
    ],
    tasks: [
      {
        id: generateId('task'),
        title: 'Explore the Nova app',
        description: 'Check out Chat, Tasks, Notes, and Calendar.',
        status: 'todo',
        priority: 'medium',
        createdAt: now(),
        updatedAt: now(),
      },
    ],
    notes: [
      {
        id: generateId('note'),
        title: 'Getting started',
        content: '# Welcome\n\nThis is your first note. **Markdown** is supported.',
        createdAt: now(),
        updatedAt: now(),
      },
    ],
    calendarEvents: [],
  };
}

let db: DbShape | null = null;
let hydrating: Promise<DbShape> | null = null;

async function persist(): Promise<void> {
  if (db) await writeJSON(DB_KEY, db);
}

export async function hydrateDb(): Promise<DbShape> {
  if (db) return db;
  if (hydrating) return hydrating;

  hydrating = (async () => {
    const existing = await readJSON<DbShape>(DB_KEY);
    db = existing ?? seedDb();
    if (!existing) await persist();
    return db;
  })();

  return hydrating;
}

const NETWORK_DELAY_MS = 250;

function delay<T>(value: T): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(value), NETWORK_DELAY_MS));
}

// --- Profile ------------------------------------------------------------
// Personal-use build: there is exactly one profile, auto-created on first
// launch. No login/accounts system - see the master build prompt's
// personal-use scope principle.

export async function getProfile(): Promise<Profile> {
  const state = await hydrateDb();
  return delay(state.profile);
}

export async function updateProfile(patch: Partial<Profile>): Promise<Profile> {
  const state = await hydrateDb();
  state.profile = { ...state.profile, ...patch };
  await persist();
  return delay(state.profile);
}

// --- Organizations ----------------------------------------------------------

export async function listOrganizations(): Promise<Organization[]> {
  const state = await hydrateDb();
  // Copy before handing off to the artificial delay() below - otherwise a
  // write that lands on the underlying array/object while this "response"
  // is still in flight would mutate it out from under the caller, since
  // delay() just resolves the same reference later rather than a snapshot.
  return delay([...state.organizations]);
}

// --- Conversations & messages ------------------------------------------------

export async function listConversations(): Promise<Conversation[]> {
  const state = await hydrateDb();
  return delay(
    [...state.conversations].sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1))
  );
}

export async function getConversation(id: string): Promise<Conversation | undefined> {
  const state = await hydrateDb();
  return delay(state.conversations.find((c) => c.id === id));
}

export async function listMessages(conversationId: string): Promise<Message[]> {
  const state = await hydrateDb();
  // Copy for the same reason as listOrganizations() above - appendMessage()
  // pushes onto this exact array, and without a copy a send that lands
  // while this call is still "in flight" would resolve with messages
  // appended after the read was supposed to have happened.
  return delay([...(state.messagesByConversation[conversationId] ?? [])]);
}

export async function createConversation(title: string): Promise<Conversation> {
  const state = await hydrateDb();
  const conversation: Conversation = {
    id: generateId('conv'),
    title,
    pinned: false,
    createdAt: now(),
    updatedAt: now(),
  };
  state.conversations.unshift(conversation);
  state.messagesByConversation[conversation.id] = [];
  await persist();
  return delay(conversation);
}

export async function togglePinConversation(id: string): Promise<void> {
  const state = await hydrateDb();
  const conversation = state.conversations.find((c) => c.id === id);
  if (conversation) conversation.pinned = !conversation.pinned;
  await persist();
}

export async function deleteConversation(id: string): Promise<void> {
  const state = await hydrateDb();
  state.conversations = state.conversations.filter((c) => c.id !== id);
  delete state.messagesByConversation[id];
  await persist();
}

export async function appendMessage(
  conversationId: string,
  message: Omit<Message, 'id' | 'conversationId' | 'createdAt'>
): Promise<Message> {
  const state = await hydrateDb();
  const full: Message = {
    ...message,
    id: generateId('msg'),
    conversationId,
    createdAt: now(),
  };
  const list = state.messagesByConversation[conversationId] ?? [];
  list.push(full);
  state.messagesByConversation[conversationId] = list;

  const conversation = state.conversations.find((c) => c.id === conversationId);
  if (conversation) {
    conversation.updatedAt = now();
    conversation.lastMessagePreview = full.content.slice(0, 120);
  }
  await persist();
  return full;
}

export async function updateMessageContent(conversationId: string, messageId: string, content: string): Promise<void> {
  const state = await hydrateDb();
  const list = state.messagesByConversation[conversationId] ?? [];
  const message = list.find((m) => m.id === messageId);
  if (message) message.content = content;
  await persist();
}

export async function deleteMessagesFrom(conversationId: string, messageId: string): Promise<void> {
  const state = await hydrateDb();
  const list = state.messagesByConversation[conversationId] ?? [];
  const index = list.findIndex((m) => m.id === messageId);
  if (index !== -1) {
    state.messagesByConversation[conversationId] = list.slice(0, index);
  }
  await persist();
}

// --- Memory -----------------------------------------------------------------

export async function listMemories(): Promise<MemoryItem[]> {
  const state = await hydrateDb();
  return delay([...state.memories].sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1)));
}

export async function addMemory(summary: string, sourceConversationId?: string): Promise<MemoryItem> {
  const state = await hydrateDb();
  const memory: MemoryItem = { id: generateId('mem'), summary, sourceConversationId, createdAt: now() };
  state.memories.unshift(memory);
  await persist();
  return memory;
}

// --- Tasks --------------------------------------------------------------------

export async function listTasks(): Promise<Task[]> {
  const state = await hydrateDb();
  return delay([...state.tasks]);
}

export async function createTask(input: Pick<Task, 'title' | 'description' | 'priority' | 'dueDate'>): Promise<Task> {
  const state = await hydrateDb();
  const task: Task = {
    id: generateId('task'),
    status: 'todo',
    createdAt: now(),
    updatedAt: now(),
    ...input,
  };
  state.tasks.unshift(task);
  await persist();
  return delay(task);
}

export async function updateTask(id: string, patch: Partial<Task>): Promise<Task> {
  const state = await hydrateDb();
  const task = state.tasks.find((t) => t.id === id);
  if (!task) throw new Error('Task not found');
  Object.assign(task, patch, { updatedAt: now() });
  await persist();
  return delay(task);
}

export async function setTaskStatus(id: string, status: TaskStatus): Promise<void> {
  await updateTask(id, { status });
}

export async function deleteTask(id: string): Promise<void> {
  const state = await hydrateDb();
  state.tasks = state.tasks.filter((t) => t.id !== id);
  await persist();
}

// --- Notes ----------------------------------------------------------------

export async function listNotes(): Promise<Note[]> {
  const state = await hydrateDb();
  return delay([...state.notes].sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1)));
}

export async function getNote(id: string): Promise<Note | undefined> {
  const state = await hydrateDb();
  return delay(state.notes.find((n) => n.id === id));
}

export async function createNote(title: string, content = ''): Promise<Note> {
  const state = await hydrateDb();
  const note: Note = { id: generateId('note'), title, content, createdAt: now(), updatedAt: now() };
  state.notes.unshift(note);
  await persist();
  return delay(note);
}

export async function updateNote(id: string, patch: Partial<Pick<Note, 'title' | 'content'>>): Promise<Note> {
  const state = await hydrateDb();
  const note = state.notes.find((n) => n.id === id);
  if (!note) throw new Error('Note not found');
  Object.assign(note, patch, { updatedAt: now() });
  await persist();
  return delay(note);
}

export async function deleteNote(id: string): Promise<void> {
  const state = await hydrateDb();
  state.notes = state.notes.filter((n) => n.id !== id);
  await persist();
}

// --- Calendar ---------------------------------------------------------------

export async function listCalendarEvents(): Promise<CalendarEvent[]> {
  const state = await hydrateDb();
  return delay([...state.calendarEvents].sort((a, b) => (a.startsAt < b.startsAt ? -1 : 1)));
}

export async function createCalendarEvent(
  input: Pick<CalendarEvent, 'title' | 'description' | 'startsAt' | 'endsAt' | 'location'>
): Promise<CalendarEvent> {
  const state = await hydrateDb();
  const event: CalendarEvent = { id: generateId('evt'), createdAt: now(), ...input };
  state.calendarEvents.push(event);
  await persist();
  return delay(event);
}

export async function updateCalendarEvent(id: string, patch: Partial<CalendarEvent>): Promise<CalendarEvent> {
  const state = await hydrateDb();
  const event = state.calendarEvents.find((e) => e.id === id);
  if (!event) throw new Error('Event not found');
  Object.assign(event, patch);
  await persist();
  return delay(event);
}

export async function deleteCalendarEvent(id: string): Promise<void> {
  const state = await hydrateDb();
  state.calendarEvents = state.calendarEvents.filter((e) => e.id !== id);
  await persist();
}
