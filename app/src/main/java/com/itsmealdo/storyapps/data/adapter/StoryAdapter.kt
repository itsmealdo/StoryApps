package com.itsmealdo.storyapps.data.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.itsmealdo.storyapps.data.local.entity.StoryList
import com.itsmealdo.storyapps.databinding.PostItemBinding

class StoryAdapter(private val context: Context, private val storyLists: List<StoryList>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = storyLists[position]
        holder.binding.tvUsername.text = story.name
        holder.binding.tvCaption.text = story.description

        Glide.with(holder.itemView.context)
            .load(story.photoUrl)
            .into(holder.binding.imgPost)

        holder.binding.cardStory.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra(DetailActivity.NAME_EXTRA, story.name)
            intent.putExtra(DetailActivity.DESCRIPTION_EXTRA, story.description)
            intent.putExtra(DetailActivity.IMAGE_URL_EXTRA, story.photoUrl)

            val optionsCompat: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                holder.itemView.context as Activity,
                Pair(holder.binding.imgPost, "picture"),
                Pair(holder.binding.tvCaption, "description")
            )

            holder.itemView.context.startActivity(intent, optionsCompat.toBundle())
        }
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root)

}