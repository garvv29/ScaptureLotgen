package com.seedtrac.lotgen.retrofit;

import com.seedtrac.lotgen.parser.actbarcodelist.ActBarcodeListResponse;
import com.seedtrac.lotgen.parser.activationsubmit.ActivationSubmitResponse;
import com.seedtrac.lotgen.parser.activationtrlist.ActivationTrListResponse;
import com.seedtrac.lotgen.parser.actlotlist.ActLotListResponse;
import com.seedtrac.lotgen.parser.actremarklist.ActRemarksListResponse;
import com.seedtrac.lotgen.parser.binlist.BinListResponse;
import com.seedtrac.lotgen.parser.dashboarddata.DashboardDataResponse;
import com.seedtrac.lotgen.parser.gsbarcodeinfo.GsBarcodeInfoResponse;
import com.seedtrac.lotgen.parser.labelprintingbarcodelist.LabelPrintingBarList;
import com.seedtrac.lotgen.parser.loadinglist.LoadingListResponse;
import com.seedtrac.lotgen.parser.loadingtrinfo.LoadingTrInfoResponse;
import com.seedtrac.lotgen.parser.loadingtrpendinglist.LoadingTrPendingListResponse;
import com.seedtrac.lotgen.parser.login.LoginResponse;
import com.seedtrac.lotgen.parser.lotinfo.LotInfoResponse;
import com.seedtrac.lotgen.parser.lotrecsubmit.LotRecSubmitSuccess;
import com.seedtrac.lotgen.parser.pendinglotreport.PendingLotListReportResponse;
import com.seedtrac.lotgen.parser.printlabel.PrintLabelInfo;
import com.seedtrac.lotgen.parser.recpendinglotlist.RecPendingLotListResponse;
import com.seedtrac.lotgen.parser.submitsuccess.SubmitSuccessResponse;
import com.seedtrac.lotgen.parser.whlist.WhListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("login.php")
    Call<LoginResponse> loginAuth(@Query("mobile1") String mobile, @Query("password") String password);

    @GET("acthomeplist.php")
    Call<ActivationTrListResponse> getActivationTrList(@Query("mobile1") String mobile, @Query("scode") String scode);

    @GET("actsetup.php")
    Call<ActivationSubmitResponse> submitActivationForm(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                         @Query("lotno") String lot, @Query("harvestdate") String harvestDate,
                                                         @Query("totbags") String bags, @Query("totqty") String qty,
                                                         @Query("tare") String tare, @Query("gotstatus") String gotStatus,
                                                         @Query("moisture") String moisture, @Query("remarks") String remarks,
                                                         @Query("prodgrade") String prodGrade);
    @GET("lotinfo.php")
    Call<LotInfoResponse> getLotInfo(@Query("mobile1") String mobile, @Query("scode") String scode,
                                     @Query("lotno") String lot);

    @GET("actlotlist.php")
    Call<ActLotListResponse> getLotList(@Query("mobile1") String mobile, @Query("scode") String scode);

    @GET("reclotlist.php")
    Call<ActLotListResponse> getRecLotList(@Query("mobile1") String mobile, @Query("scode") String scode,
                                           @Query("farmerid") String farmerID);

    @GET("remarkslist.php")
    Call<ActRemarksListResponse> getRemarksList(@Query("scode") String scode, @Query("type") String type);

    @GET("actbarlist.php")
    Call<ActBarcodeListResponse> getActBarList(@Query("scode") String scode, @Query("trid") String trid);

    @GET("barrcodechk.php")
    Call<SubmitSuccessResponse> checkBagCode(@Query("scode") String scode,
                                             @Query("trid") String trid, @Query("qrcode") String bagCode);

    @GET("scanqrcode.php")
    Call<SubmitSuccessResponse> storeBagCode(@Query("mobile1") String mobile, @Query("scode") String scode,
                                             @Query("trid") String trid, @Query("qrcode") String bagCode,
                                             @Query("action") String action, @Query("trtype") String type,
                                             @Query("weight") String weight);

    @GET("actsetupfinal.php")
    Call<SubmitSuccessResponse> actSetupFinalSubmit(@Query("scode") String scode, @Query("trid") String trid);


    @GET("loadsetup.php")
    Call<ActivationSubmitResponse> submitLoadingSetupForm(@Query("mobile1") String mobile1, @Query("scode") String scode,
                                                        @Query("tname") String tname, @Query("vehno") String vehno,
                                                        @Query("lrno") String lrNumber, @Query("drivername") String driverName,
                                                        @Query("driverno") String mobile, @Query("destination") String destination, @Query("dispdate") String dispatchDate);

    @GET("loadinglist.php")
    Call<LoadingListResponse> getLoadingList(@Query("scode") String scode, @Query("trid") String trid);

    @GET("loadingtrandetails.php")
    Call<LoadingTrInfoResponse> getTransInfo(@Query("scode") String scode, @Query("trid") String trid);

    @GET("loadinghomeplist.php")
    Call<LoadingTrPendingListResponse> getLoadingPendingList(@Query("mobile1") String mobile, @Query("scode") String scode);

    @GET("loadingfinal.php")
    Call<SubmitSuccessResponse> loadingFinalSubmit(@Query("scode") String scode, @Query("trid") String trid, @Query("vehiclepickup") String vehiclePickup);

    @GET("whlist.php")
    Call<WhListResponse> getWhList(@Query("mobile1") String mobile, @Query("scode") String scode);

    @GET("binlist.php")
    Call<BinListResponse> getBinList(@Query("mobile1") String mobile, @Query("scode") String scode, @Query("whid") Integer whId);

    @GET("lotrecpost.php")
    Call<LotRecSubmitSuccess> submitReceiveForm(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                @Query("lotno") String lot, @Query("harvestdate") String harvestDate,
                                                @Query("nob") String bags, @Query("trid") String trid,
                                                @Query("whid") String tare, @Query("binid") String gotStatus,
                                                @Query("trantype") String tagType);

    @GET("lotrecupd.php")
    Call<LotRecSubmitSuccess> updateReceiveForm(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                @Query("lotno") String lot, @Query("harvestdate") String harvestDate,
                                                @Query("nob") String bags, @Query("trid") String trid,
                                                @Query("whid") String tare, @Query("binid") String gotStatus,
                                                @Query("rowid") Integer rowid, @Query("tagType") String tagType);

    @GET("rectrlotlist.php")
    Call<RecPendingLotListResponse> getLotRecPendingList(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                         @Query("trid") String trid);

    @GET("lotrecfinal.php")
    Call<SubmitSuccessResponse> submitRecList(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                         @Query("trid") String trid);

    @GET("dashboard.php")
    Call<DashboardDataResponse> getDashboardData(@Query("mobile1") String mobile, @Query("scode") String scode);

    @GET("dashbpending.php")
    Call<PendingLotListReportResponse> getPendingLotList(@Query("mobile1") String mobile1, @Query("scode") String scode, @Query("type") String type);

    //Label Printing
    @GET("qrcodegen.php")
    Call<PrintLabelInfo> getLabelPrintData(@Query("mobile1") String mobile1, @Query("scode") String scode,
                                           @Query("lotno") String lotno, @Query("weight") String weight);

    @GET("recqrlist.php")
    Call<LabelPrintingBarList> getPrintBarList(@Query("scode") String scode, @Query("lotno") String lotno);

    // Guard Sample
    @GET("gslotlist.php")
    Call<ActLotListResponse> getGsLotList(@Query("mobile1") String mobile, @Query("scode") String scode);

    @GET("gsbarrcodechk.php")
    Call<SubmitSuccessResponse> checkGsBarcode(@Query("mobile1") String mobile1, @Query("scode") String scode,
                                               @Query("qrcode") String qrcode);

    @GET("gsdatapost.php")
    Call<SubmitSuccessResponse> updateGuardSampleDetails(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                @Query("qrcode") String qrcode, @Query("lotno") String lotno,
                                                @Query("whid") String whid, @Query("binid") String binid);

    @GET("gsdataupd.php")
    Call<SubmitSuccessResponse> updateGsSLOCDetails(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                    @Query("qrcode") String qrcode, @Query("whid") String whid,
                                                    @Query("binid") String binid);

    @GET("gsbarcodeinfo.php")
    Call<GsBarcodeInfoResponse> getGsBarcodeInfo(@Query("mobile1") String mobile, @Query("scode") String scode,
                                                 @Query("qrcode") String qrcode);


}
