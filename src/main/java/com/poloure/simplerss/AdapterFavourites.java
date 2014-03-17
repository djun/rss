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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.LinkedHashSet;
import java.util.Set;

class AdapterFavourites extends BaseAdapter
{
   private static final int TYPE_PLAIN = 0;
   private static final int TYPE_IMAGE = 1;
   private static final int TYPE_IMAGE_SANS_DESCRIPTION = 2;
   private static final int TYPE_PLAIN_SANS_DESCRIPTION = 3;
   private final Context m_context;
   Set<FeedItem> m_feedItems = new LinkedHashSet<FeedItem>(0);

   AdapterFavourites(Context context)
   {
      m_context = context;
      LinkedHashSet<FeedItem> list = (LinkedHashSet<FeedItem>) Read.object(context, Read.FAVOURITES);
      if(null != list)
      {
         m_feedItems = new LinkedHashSet<FeedItem>(list);
      }
   }

   @Override
   public
   int getItemViewType(int position)
   {
      FeedItem feedItem = m_feedItems.toArray(new FeedItem[m_feedItems.size()])[position];

      boolean isImage = !feedItem.m_imageLink.isEmpty();
      boolean isDes = !feedItem.m_desLines[0].isEmpty();

      return isImage ? isDes ? TYPE_IMAGE : TYPE_IMAGE_SANS_DESCRIPTION : isDes ? TYPE_PLAIN : TYPE_PLAIN_SANS_DESCRIPTION;
   }

   @Override
   public
   int getCount()
   {
      return m_feedItems.size();
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 4;
   }

   @Override
   public
   Object getItem(int position)
   {
      return m_feedItems.toArray(new FeedItem[m_feedItems.size()])[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      int viewType = getItemViewType(position);

      boolean hasImg = TYPE_IMAGE == viewType || TYPE_IMAGE_SANS_DESCRIPTION == viewType;

      ViewFeedItem view = null != convertView ? (ViewFeedItem) convertView : new ViewFeedItem(m_context, viewType);
      FeedItem item = m_feedItems.toArray(new FeedItem[m_feedItems.size()])[position];

      /* If the recycled view is the view we want, keep it. */
      if(null != convertView)
      {
         if(item.m_time.equals(view.m_item.m_time))
         {
            return view;
         }
      }

      view.setAlpha(1.0F);
      view.setBackgroundResource(R.drawable.selector_white);
      view.m_item = item;
      view.m_hasImage = hasImg;

      /* If the view was an image, load the image. */
      if(hasImg)
      {
         view.setBitmap(null);
         view.setTag(item.m_time);
         AsyncLoadImage.newInstance(view, item.m_imageName, item.m_time);
      }

      return view;
   }
}
