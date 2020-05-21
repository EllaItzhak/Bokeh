package com.example.ellai.bokeh2;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BlurImageFragment extends Fragment  implements SeekBar.OnSeekBarChangeListener{

    private BlurImageFragmentListener listener;

    @BindView(R.id.seekbar_blur)
    SeekBar seekBarBlur;

    public void setListener(BlurImageFragmentListener listener) {
        this.listener = listener;
    }


    public BlurImageFragment() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment BlurImageFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static BlurImageFragment newInstance(String param1, String param2) {
//        BlurImageFragment fragment = new BlurImageFragment();
////        Bundle args = new Bundle();
////        args.putString(ARG_PARAM1, param1);
////        args.putString(ARG_PARAM2, param2);
////        fragment.setArguments(args);
//       return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_blur_image, container, false);

        ButterKnife.bind(this, view);

        // keeping brightness value b/w -100 / +100
        seekBarBlur.setMax(200);
        seekBarBlur.setProgress(100);

        seekBarBlur.setOnSeekBarChangeListener(this);

        // Inflate the layout for this fragment
        return view;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (listener != null) {
//            listener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            listener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        listener = null;
//    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (listener != null) {

            if (seekBar.getId() == R.id.seekbar_blur) {
                // blur values are b/w -100 to +100
                listener.onBlurChanged(progress - 100);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onBlurStarted();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null)
            listener.onBlurCompleted();
    }

    public void resetControls() {
        seekBarBlur.setProgress(100);
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public interface BlurImageFragmentListener {
        void onBlurChanged(int blur);

        void onBlurStarted();

        void onBlurCompleted();
    }

}
