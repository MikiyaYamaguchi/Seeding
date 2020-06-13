package to.msn.wings.triangle;

import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    public List iThread;
    public List iThreadCategory;
    public String userName;

    public int pinCount = 0;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView ThreadNameView;
        ImageView ThreadCategoryImage;
        ImageView PinIcon;

        ViewHolder(View v){
            super(v);
            ThreadNameView = (TextView)v.findViewById(R.id.text_view);
            ThreadCategoryImage = (ImageView)v.findViewById(R.id.thread_category_icon);
            PinIcon = (ImageView)v.findViewById(R.id.pin_icon);
        }
    }

    MyAdapter(List itemThread, List itemThreadCategory, String user) {
        this.iThread = itemThread;
        this.iThreadCategory = itemThreadCategory;
        this.userName = user;
    }

    //新しいViewの作成（LayoutManagerによって呼び出される）
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //新しいView作成
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        //recyclerViewのitemViewクリック処理
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int position = holder.getAdapterPosition();//itemViewのposition取得
                String[] send_info = {userName, (String) iThread.get(position)};
                Context context = v.getContext();
                Intent intent = new Intent(context,ChatClientActivity.class);
                intent.putExtra(ChatClientActivity.EXTRA_NAME,send_info);
                context.startActivity(intent);
            }
        });

        return holder;
    }

    //Viewのコンテンツを置き換える（LayoutManagerによって呼び出される）
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.ThreadNameView.setText((CharSequence) iThread.get(position));
        String category = (String) iThreadCategory.get(position);
        System.out.println(category);
        switch (category){
            case "仕事":
                holder.ThreadCategoryImage.setImageResource(R.drawable.ic_work);
                break;
            case "趣味":
                holder.ThreadCategoryImage.setImageResource(R.drawable.ic_hoby);
                break;
            case "遊び":
                holder.ThreadCategoryImage.setImageResource(R.drawable.ic_play);
                break;
            case "ニュース":
                holder.ThreadCategoryImage.setImageResource(R.drawable.ic_news);
                break;
                default:
                    holder.ThreadCategoryImage.setImageResource(R.drawable.ic_chat);
                    break;
        }
    }

    @Override
    public int getItemCount() {
        return iThread.size();
    }
}
