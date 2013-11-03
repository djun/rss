package com.poloure.simplerss;

import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class AsyncRefreshPage extends AsyncTask<Integer, Object, Void>
{
   private static final int MAX_DESCRIPTION_LENGTH = 360;
   private static final int MIN_DESCRIPTION_LENGTH = 8;
   private static final int MIN_IMAGE_WIDTH        = 32;
   private final String   m_applicationFolder;
   private final ListView m_listView;
   private final boolean  m_isAllTag;

   private
   AsyncRefreshPage(ListView listView, String applicationFolder, boolean isAllTag)
   {
      m_listView = listView;
      m_applicationFolder = applicationFolder;
      m_isAllTag = isAllTag;
   }

   static
   void newInstance(int pageNumber, ListView listView, String storage, boolean isAllTag)
   {
      AsyncTask<Integer, Object, Void> task = new AsyncRefreshPage(listView, storage, isAllTag);

      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(THREAD_POOL_EXECUTOR, pageNumber);
      }
      else
      {
         task.execute(pageNumber);
      }
   }

   @Override
   protected
   Void doInBackground(Integer... page)
   {
      int pageNumber = page[0];
      String tag = PagerAdapterFeeds.getTagsArray()[pageNumber];

      String[][] feedsIndex = Read.csvFile(Read.INDEX, m_applicationFolder, 'f', 't');
      if(0 == feedsIndex.length)
      {
         return null;
      }
      String[] feedNames = feedsIndex[0];
      String[] feedTags = feedsIndex[1];

      Comparator<Long> reverse = Collections.reverseOrder();
      Map<Long, FeedItem> map = new TreeMap<Long, FeedItem>(reverse);

      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();

      int feedsLength = feedNames.length;
      for(int j = 0; j < feedsLength; j++)
      {
         if(m_isAllTag || feedTags[j].contains(tag))
         {
            String[][] content = Read.csvFile(
                  feedNames[j] + File.separatorChar + ServiceUpdate.CONTENT, m_applicationFolder,
                  't', 'd', 'l', 'i', 'w', 'h', 'p');
            if(0 == content.length)
            {
               return null;
            }
            String[] titles = content[0];
            String[] descriptions = content[1];
            String[] links = content[2];
            String[] images = content[3];
            String[] widths = content[4];
            String[] heights = content[5];
            String[] times = content[6];

            int timesLength = times.length;
            for(int i = 0; i < timesLength; i++)
            {
               /* Edit the data. */
               if(null != images[i])
               {

                  if(MIN_IMAGE_WIDTH < (null == widths[i] || 0 == widths[i].length()
                        ? 0
                        : Integer.parseInt(widths[i])))
                  {
                     int lastSlash = images[i].lastIndexOf(File.separatorChar) + 1;
                     images[i] = feedNames[j] + File.separatorChar + ServiceUpdate.THUMBNAIL_DIR +
                           images[i].substring(lastSlash);
                  }
                  else
                  {
                     images[i] = "";
                     widths[i] = "";
                     heights[i] = "";
                  }
               }

               if(null == descriptions[i] || MIN_DESCRIPTION_LENGTH > descriptions[i].length())
               {
                  descriptions[i] = "";
               }
               else if(MAX_DESCRIPTION_LENGTH <= descriptions[i].length())
               {
                  descriptions[i] = descriptions[i].substring(0, MAX_DESCRIPTION_LENGTH);
               }
               if(null == titles[i])
               {
                  titles[i] = "";
               }

               FeedItem data = new FeedItem();
               data.m_itemTitle = titles[i];
               data.m_itemUrl = links[i];
               data.m_itemDescription = descriptions[i];
               data.m_itemImage = images[i];
               data.m_itemTime = Long.parseLong(times[i]);

               data.m_imageWidth = null == widths[i] || 0 == widths[i].length()
                     ? 0
                     : Integer.parseInt(widths[i]);

               data.m_imageHeight = null == heights[i] || 0 == heights[i].length()
                     ? 0
                     : Integer.parseInt(heights[i]);

               /* Do not add duplicates. */
               if(!adapterTag.m_times.contains(data.m_itemTime))
               {
                  map.put(data.m_itemTime, data);
               }
            }
         }
      }

      /* Do not count items as Read while we are updating the list. */
      adapterTag.m_isReadingItems = false;

      int mapSize = map.size();
      Collection<FeedItem> itemCollection = map.values();
      List<Long> longList = Arrays.asList(map.keySet().toArray(new Long[mapSize]));

      if(0 < itemCollection.size())
      {
         publishProgress(itemCollection, longList);
      }
      return null;
   }

   @Override
   protected
   void onPostExecute(Void result)
   {
      /* Resume Read item checking. */
      Adapter adapterTag = m_listView.getAdapter();
      ((AdapterTags) adapterTag).m_isReadingItems = true;
   }

   @Override
   protected
   void onProgressUpdate(Object... values)
   {

      int top = 0;
      int index = 0;
      long timeBefore = 0L;
      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();

      /* If these are the first items to be added to the list. */
      if(0 == m_listView.getCount())
      {
         m_listView.setVisibility(View.INVISIBLE);
      }
      /* Find the exact mPosition in the list. */
      else
      {
         /* Get the time of the top item. */
         index = m_listView.getFirstVisiblePosition();
         FeedItem topItem = (FeedItem) adapterTag.getItem(index);
         timeBefore = topItem.m_itemTime;

         View v = m_listView.getChildAt(0);
         top = null == v ? 0 : v.getTop();
         if(0 != top)
         {
            View childAt = m_listView.getChildAt(1);
            if(null != childAt)
            {
               top = childAt.getTop();
            }
         }
      }

      adapterTag.prependArray(values[0], values[1]);
      adapterTag.notifyDataSetChanged();

      /* If this was the first time loading the tag data, jump to the latest unread item. */
      if(m_listView.isShown())
      {
         /* We now need to find the position of the item with the time timeBefore. */
         /* NOTE Do not change anything in itemList. */
         List<Long> timeList = adapterTag.m_times;
         int timeListSize = timeList.size();
         int i = 0;
         while(i < timeListSize && 0 == index)
         {
            Long time = timeList.get(i);
            if(time == timeBefore)
            {
               index = i + 1;
            }
            i++;
         }

         int listViewPaddingTop = m_listView.getPaddingTop();
         m_listView.setSelectionFromTop(index, top - listViewPaddingTop);
      }
      else
      {
         FeedsActivity.gotoLatestUnread(m_listView);
      }
   }
}
