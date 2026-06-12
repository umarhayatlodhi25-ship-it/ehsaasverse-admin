import { supabase } from '../supabase/config';
import { v4 as uuidv4 } from 'uuid';

export const getPhotoShayaris = async (categoryName = null) => {
  let q = supabase.from('image_shayari').select('*').order('created_at', { ascending: false });
  if (categoryName) {
    q = q.eq('category', categoryName);
  }
  const { data, error } = await q;
  if (error) {
    console.error("Supabase fetch error:", error);
    return [];
  }
  return data.map(item => ({
    id: item.id,
    imageUrl: item.image_url,
    categoryId: item.category,
    categoryName: item.category,
    createdAt: item.created_at
  }));
};

export const uploadImage = async (file) => {
  const fileExt = file.name.split('.').pop();
  const fileName = `${uuidv4()}.${fileExt}`;

  const { error } = await supabase.storage
    .from('ehsaasverse-images')
    .upload(fileName, file);

  if (error) {
    console.error("Supabase storage error:", error);
    throw error;
  }

  const { data: { publicUrl } } = supabase.storage
    .from('ehsaasverse-images')
    .getPublicUrl(fileName);

  return publicUrl;
};

export const addPhotoShayari = async (data) => {
  const { data: insertedData, error } = await supabase
    .from('image_shayari')
    .insert([{
      title: 'Untitled',
      category: data.categoryName,
      image_url: data.imageUrl,
      status: data.status || 'published'
    }]);

  if (error) {
    console.error("Supabase insert error:", error);
    throw error;
  }
  return insertedData;
};

export const deletePhotoShayari = async (id, imageUrl) => {
  if (imageUrl) {
    const fileName = imageUrl.split('/').pop();
    await supabase.storage.from('ehsaasverse-images').remove([fileName]);
  }
  const { error } = await supabase.from('image_shayari').delete().eq('id', id);
  if (error) {
    console.error("Supabase delete error:", error);
    throw error;
  }
};
