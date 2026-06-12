-- 1. Create Image Shayari Table
CREATE TABLE image_shayari (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title TEXT,
    category TEXT DEFAULT 'General',
    image_url TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    status TEXT DEFAULT 'published'
);

-- 2. Create Categories Table (if managed in Supabase too)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 3. Enable Row Level Security (RLS)
ALTER TABLE image_shayari ENABLE ROW LEVEL SECURITY;

-- 4. Policies for image_shayari
CREATE POLICY "Public Read Access" ON image_shayari FOR SELECT USING (true);
CREATE POLICY "Admin Insert" ON image_shayari FOR INSERT WITH CHECK (auth.email() = 'lodhitools@gmail.com');
CREATE POLICY "Admin Update" ON image_shayari FOR UPDATE USING (auth.email() = 'lodhitools@gmail.com');
CREATE POLICY "Admin Delete" ON image_shayari FOR DELETE USING (auth.email() = 'lodhitools@gmail.com');

-- 5. Storage Policies (Bucket: ehsaasverse-images)
-- Note: Create the bucket 'ehsaasverse-images' first in Supabase Dashboard.
-- Allow public to read images
CREATE POLICY "Public Image Access" ON storage.objects FOR SELECT USING (bucket_id = 'ehsaasverse-images');
-- Allow Admin to upload/manage
CREATE POLICY "Admin Image Upload" ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'ehsaasverse-images' AND auth.email() = 'lodhitools@gmail.com');
CREATE POLICY "Admin Image Delete" ON storage.objects FOR DELETE USING (bucket_id = 'ehsaasverse-images' AND auth.email() = 'lodhitools@gmail.com');
