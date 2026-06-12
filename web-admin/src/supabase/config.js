import { createClient } from '@supabase/supabase-js'

const supabaseUrl = "https://jgdrdtirmuvkoznfuuog.supabase.co"
const supabaseKey = "sb_publishable_23VXzWVp2ofyGLegPbOMBw_rElW679g"

export const supabase = createClient(supabaseUrl, supabaseKey)
