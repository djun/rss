/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

class AsyncLoadImage extends AsyncTask<String, Void, Bitmap>
{
   private static final int IMAGE_FADE_IN_DURATION = 166;
   private final WeakReference<ViewFeedItem> m_view;
   private final int m_viewTag;
   private final float m_opacity;
   private final Context m_context;

   private
   AsyncLoadImage(ViewFeedItem view, int viewTag, float opacity)
   {
      m_context = view.getContext();
      m_view = new WeakReference<>(view);
      m_viewTag = viewTag;
      m_opacity = opacity;
   }

   static
   void newInstance(ViewFeedItem view, String imageName, int viewTag, float opacity)
   {
      AsyncTask<String, Void, Bitmap> task = new AsyncLoadImage(view, viewTag, opacity);

      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageName);
   }

   @Override
   protected
   Bitmap doInBackground(String... params)
   {
      View viewReference = m_view.get();
      if(null == viewReference)
      {
         cancel(true);
         return null;
      }

      try(FileInputStream in = m_context.openFileInput(params[0]))
      {
         return BitmapFactory.decodeStream(in);
      }
      catch(IOException ignored)
      {
         return null;
      }
   }

   @Override
   protected
   void onPostExecute(Bitmap result)
   {
      if(isCancelled())
      {
         return;
      }

      ViewFeedItem view = m_view.get();
      if(null != view && (Integer) view.getTag() == m_viewTag && null != result)
      {
         AlphaAnimation animation = new AlphaAnimation(0.0F, m_opacity);
         animation.setDuration(IMAGE_FADE_IN_DURATION);
         animation.setFillAfter(true);
         view.setBitmap(result);
         view.setAnimation(animation);
      }
   }
}
