package com.poloure.simplerss;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

class FragmentManageTags extends ListFragment
{
   public static
   void AsyncCompatManageTagsRefresh(ListView listView, ListAdapter listAdapter, Context context)
   {
      AsyncManageTagsRefresh task = new AsyncManageTagsRefresh(listView, listAdapter, context);
      if(Constants.HONEYCOMB)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView listView = getListView();
      Context context = getActivity();
      ListAdapter listAdapter = new AdapterManagerTags(context);
      setListAdapter(listAdapter);

      AsyncCompatManageTagsRefresh(listView, listAdapter, context);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_cards, container, false);
   }

}