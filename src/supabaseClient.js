import { createClient } from "@supabase/supabase-js";

// REPLACE THESE WITH YOUR ACTUAL SUPABASE VALUES
const SUPABASE_URL = "https://ncbfpwjzbbtizksojgzp.supabase.co";
const SUPABASE_PUBLIC_KEY = "sb_publishable_d3qBi3986meqcK9K0Jngbg_W0MsOXVa";

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLIC_KEY);
