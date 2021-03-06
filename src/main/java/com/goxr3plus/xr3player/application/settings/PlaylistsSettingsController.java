package main.java.com.goxr3plus.xr3player.application.settings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTabPane;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import main.java.com.goxr3plus.xr3player.application.Main;
import main.java.com.goxr3plus.xr3player.application.tools.ActionTool;
import main.java.com.goxr3plus.xr3player.application.tools.InfoTool;
import main.java.com.goxr3plus.xr3player.application.tools.JavaFXTools;
import main.java.com.goxr3plus.xr3player.application.tools.NotificationType;

/**
 * 
 * .
 * 
 * @author GOXR3PLUS
 */
public class PlaylistsSettingsController extends BorderPane {
	
	/** -----------------------------------------------------. */
	
	@FXML
	private JFXTabPane innerTabPane;
	
	@FXML
	private ToggleGroup playedFilesDetectionGroup;
	
	@FXML
	private JFXButton clearPlayedFilesHistory;
	
	@FXML
	private MenuButton totalItemsMenuButton;
	
	@FXML
	private ToggleGroup totalFilesShownGroup;
	
	@FXML
	private ToggleGroup whichFilesToShowGenerally;
	
	@FXML
	private ToggleGroup filesToShowUnderFolders;
	
	@FXML
	private JFXCheckBox instantSearch;
	
	@FXML
	private ToggleGroup fileSearchGroup;
	
	// -------------------------------------------------------------
	
	/** The logger. */
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * Constructor.
	 */
	public PlaylistsSettingsController() {
		
		// ------------------------------------FXMLLOADER ----------------------------------------
		FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.SETTINGS_FXMLS + "PlayListsSettingsController.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		
		try {
			loader.load();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "", ex);
		}
		
	}
	
	/**
	 * Called as soon as .fxml is initialized
	 */
	@FXML
	private void initialize() {
		
		//--Playlists-Settings-Search--------------
		
		//instantSearch
		instantSearch.selectedProperty().addListener(l -> {
			
			Main.dbManager.getPropertiesDb().updateProperty("PlayLists-Search-InstantSearch", String.valueOf(instantSearch.isSelected()));
			System.out.println("Instant Search Updated...");
			
		});
		
		//fileSearchGroup
		fileSearchGroup.selectedToggleProperty().addListener(listener -> Main.dbManager.getPropertiesDb().updateProperty("PlayLists-Search-FileSearchUsing",
				Integer.toString(JavaFXTools.getIndexOfSelectedToggle(fileSearchGroup))));
		
		//--Playlists-Settings-General--------------
		
		//playedFilesDetectionGroup
		playedFilesDetectionGroup.selectedToggleProperty().addListener(listener -> Main.dbManager.getPropertiesDb().updateProperty("PlayLists-General-PlayedFilesDetection",
				Integer.toString(JavaFXTools.getIndexOfSelectedToggle(playedFilesDetectionGroup))));
		
		//totalFilesShownGroup
		totalFilesShownGroup.selectedToggleProperty().addListener(listener -> {
			//Sometimes it reports a madafacka null for some reason 
			if (totalFilesShownGroup.getSelectedToggle() != null) {
				
				//Update the properties file
				Main.dbManager.getPropertiesDb().updateProperty("PlayLists-General-TotalFilesShown", Integer.toString(JavaFXTools.getIndexOfSelectedToggle(totalFilesShownGroup)));
				
				int maximumPerPlaylist = getMaximumPerPlaylist();
				
				//Update totalItemsMenuButton
				totalItemsMenuButton.setText(Integer.toString(maximumPerPlaylist));
				
				//First Update all the Libraries
				Main.libraryMode.teamViewer.getViewer().getItemsObservableList().forEach(library -> library.getSmartController().setNewMaximumPerPage(maximumPerPlaylist, true));
				
				//Secondly Update the Search Window PlayList
				Main.searchWindowSmartController.setNewMaximumPerPage(maximumPerPlaylist, true);
				
				//Thirdly Update all the XPlayers SmartController
				Main.xPlayersList.getList().stream().map(xPlayerController -> xPlayerController.getxPlayerPlayList().getSmartController())
						.forEach(controller -> controller.setNewMaximumPerPage(maximumPerPlaylist, true));
				
				//Finally all the Emotion Lists Controllers
				Main.emotionListsController.hatedMediaListController.setNewMaximumPerPage(maximumPerPlaylist, true);
				Main.emotionListsController.dislikedMediaListController.setNewMaximumPerPage(maximumPerPlaylist, true);
				Main.emotionListsController.likedMediaListController.setNewMaximumPerPage(maximumPerPlaylist, true);
				Main.emotionListsController.lovedMediaListController.setNewMaximumPerPage(maximumPerPlaylist, true);
			}
		});
		
		//clearPlayedFilesHistory
		clearPlayedFilesHistory.setOnAction(a -> {
			if (ActionTool.doQuestion(null, "Are you sure ? ", clearPlayedFilesHistory, Main.window))
				if (Main.playedSongs.clearAll(true))
					ActionTool.showNotification("Message", "Successfully cleared played files from database", Duration.millis(1500), NotificationType.INFORMATION);
				else
					ActionTool.showNotification("Message", "Problem occured trying to clear played files from database", Duration.millis(1500), NotificationType.ERROR);
		});
		
		//--------------------FOLDERS MODE---------------------------------
		
		//whichFilesToShowGenerally
		whichFilesToShowGenerally.selectedToggleProperty().addListener((observable , oldValue , newValue) -> {
			
			//Update the properties file
			Main.dbManager.getPropertiesDb().updateProperty("PlayLists-FoldersMode-WhichFilesToShowGenerally", ( (Control) newValue ).getTooltip().getText());
			
		});
		
		//filesToShowUnderFolders
		filesToShowUnderFolders.selectedToggleProperty().addListener((observable , oldValue , newValue) -> {
			
			//Update the properties file
			Main.dbManager.getPropertiesDb().updateProperty("PlayLists-FoldersMode-FilesToShowUnderFolders", ( (Control) newValue ).getTooltip().getText());
			
		});
		
		//--------------------END OF FOLDERS MODE---------------------------------
		
	}
	
	/**
	 * Restores all the settings that have to do with the category of the class
	 */
	public void restoreSettings() {
		
		//instantSearch
		instantSearch.setSelected(true);
		
		//fileSearchGroup
		JavaFXTools.selectToggleOnIndex(fileSearchGroup, 1);
		
		//totalFilesShownGroup
		JavaFXTools.selectToggleOnIndex(totalFilesShownGroup, 0);
		
		//playedFilesDetectionGroup
		JavaFXTools.selectToggleOnIndex(playedFilesDetectionGroup, 1);
	}
	
	/**
	 * The maximum items per playlist page allowed
	 * 
	 * @return The maximum items per playlist page allowed
	 */
	public int getMaximumPerPlaylist() {
		return Integer.parseInt( ( (RadioMenuItem) totalFilesShownGroup.getSelectedToggle() ).getText());
	}
	
	/**
	 * @return the instantSearch
	 */
	public JFXCheckBox getInstantSearch() {
		return instantSearch;
	}
	
	/**
	 * @return the playedFilesDetectionGroup
	 */
	public ToggleGroup getPlayedFilesDetectionGroup() {
		return playedFilesDetectionGroup;
	}
	
	/**
	 * @return the totalFilesShownGroup
	 */
	public ToggleGroup getTotalFilesShownGroup() {
		return totalFilesShownGroup;
	}
	
	/**
	 * @return the fileSearchGroup
	 */
	public ToggleGroup getFileSearchGroup() {
		return fileSearchGroup;
	}
	
	/**
	 * @return the innerTabPane
	 */
	public JFXTabPane getInnerTabPane() {
		return innerTabPane;
	}
	
	/**
	 * @return the whichFilesToShowGenerally
	 */
	public ToggleGroup getWhichFilesToShowGenerally() {
		return whichFilesToShowGenerally;
	}
	
	/**
	 * @return the filesToShowUnderFolders
	 */
	public ToggleGroup getFilesToShowUnderFolders() {
		return filesToShowUnderFolders;
	}
	
}
