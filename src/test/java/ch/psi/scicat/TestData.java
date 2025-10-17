package ch.psi.scicat;

import ch.psi.scicat.model.Dataset;
import ch.psi.scicat.model.PublishedData;
import ch.psi.scicat.model.PublishedDataStatus;
import ch.psi.scicat.model.UserInfos;
import java.time.Instant;
import java.util.List;

public class TestData {
  public static final UserInfos rocrateUser;
  public static final String validDoi = "10.000/abc";
  public static PublishedData psiPub1;
  public static Dataset psiDs1;
  public static Dataset psiDs2;
  public static Dataset psiDs3;

  static {
    rocrateUser =
        new UserInfos().setCurrentUser("rocrate").setCurrentGroups(List.of("user", "rocrate"));

    psiPub1 =
        new PublishedData()
            .setDoi("10.9999/psi_pub1")
            .setCreator(
                List.of(
                    "Elena Borisova",
                    "Goran Lovric",
                    "Arttu Mietinen",
                    "Luca Fardin",
                    "Sam Bayat",
                    "Anders Larsson",
                    "Marco Stampanoni",
                    "Johannes C. Schittny",
                    "Christian M. Schlepütz"))
            .setPublisher("PSI")
            .setPublicationYear(2020)
            .setTitle(
                "Micrometer-resolution X-ray tomographic imaging of a complete intact post mortem"
                    + " juvenile rat lung")
            .setUrl("")
            .setAbstract(
                "In the associate article to these data sets, we present an X-ray tomographic"
                    + " imaging method that is well suited for pulmonary disease studies in animal"
                    + " models, to resolve the full pathway from gas intake to gas exchange."
                    + " Current state-of-the-art synchrotron-based tomographic phase-contrast"
                    + " imaging methods allow for three-dimensional microscopic imaging data to be"
                    + " acquired non-destructively in scan times of the order of seconds with good"
                    + " soft tissue contrast. However, when studying multi-scale hierarchically"
                    + " structured objects, such as the mammalian lung, the overall sample size"
                    + " typically exceeds the field of view illuminated by the X-rays in a single"
                    + " scan, and the necessity for achieving a high spatial resolution conflicts"
                    + " with the need to image the whole sample. Several image-stitching and"
                    + " calibration techniques to achieve extended high-resolution fields of view"
                    + " have been reported, but those approaches tend to fail when imaging"
                    + " non-stable samples, thus precluding tomographic measurements of large"
                    + " biological samples, which are prone to degradation and motion during"
                    + " extended scan times. In this work, we demonstrate a full-volume"
                    + " three-dimensional reconstruction of an intact rat lung under immediate post"
                    + " mortem conditions and at an isotropic voxel size of (2.75 µm)^3. We present"
                    + " the methodology for collecting multiple local tomographies with 360 degree"
                    + " extended field of view scans followed by locally non-rigid volumetric"
                    + " stitching. Applied to the lung, it allows to resolve the entire pulmonary"
                    + " structure from the trachea down to the parenchyma in a single dataset.")
            .setDataDescription(
                "This published data collection contains three large volume datasets obtained by"
                    + " X-ray tomographic microscopy of the full juvenile rat lung structure at"
                    + " micrometer resolution. Data were collected and processed at the TOMCAT"
                    + " beamline X02DA of the Swiss Light Source. The first dataset contains the"
                    + " full scanned volume reconstruction (ca. 1.2 Tb), while a second one"
                    + " contains the same data but cropped down in size to the smallest bounding"
                    + " box encompassing the entire lung structure. The third dataset is a"
                    + " binarized version of the second one after thresholding operations to"
                    + " segment out the air volume of the lung.")
            .setResourceType("derived")
            .setPidArray(
                List.of(
                    "PID.SAMPLE.PREFIX/psi_ds1",
                    "PID.SAMPLE.PREFIX/psi_ds2",
                    "PID.SAMPLE.PREFIX/psi_ds3"))
            .setRegisteredTime(Instant.parse("2020-02-03T08:44:00.000Z"))
            .setStatus(PublishedDataStatus.REGISTERED)
            .setCreatedAt("2021-06-29T16:13:09.713Z")
            .setUpdatedAt("2021-06-29T16:13:09.717Z");

    psiDs1 =
        new Dataset()
            .setPid("PID.SAMPLE.PREFIX/psi_ds1")
            .setDatasetName("PSI-2018-R2-6_cropped_volume")
            .setDescription(
                "Full-volume high-resolution x-ray tomographic microscopy reconstruction of a"
                    + " juvenile rat lung in fresh post mortem conditions;\n"
                    + " cropped to the maximum extent of the lung structure");
    psiDs2 =
        new Dataset()
            .setPid("PID.SAMPLE.PREFIX/psi_ds2")
            .setDatasetName("PSI-2018-R2-6_segmented_cropped_volume")
            .setDescription(
                "Full-volume high-resolution x-ray tomographic microscopy reconstruction of a"
                    + " juvenile rat lung in fresh post mortem conditions;\n"
                    + " cropped to the maximum extent of the lung structure and segmented to"
                    + " separate air spaces from lung tissue");
    psiDs3 =
        new Dataset()
            .setPid("PID.SAMPLE.PREFIX/psi_ds3")
            .setDatasetName("data_final_volume_fullresolution/PSI-2018-R2-6_full_volume")
            .setDescription(
                "Full-volume high-resolution x-ray tomographic microscopy reconstruction of a"
                    + " juvenile rat lung in fresh post mortem conditions");
  }
}
