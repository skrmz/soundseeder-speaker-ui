/**
 * 
 */
package de.flexguse.soundseeder.ui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus.SessionEventBus;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

import de.flexguse.soundseeder.model.SpeakerConfiguration;
import de.flexguse.soundseeder.service.ConfigurationService;
import de.flexguse.soundseeder.service.ConfigurationServiceException;
import de.flexguse.soundseeder.ui.component.VolumeSlider;
import de.flexguse.soundseeder.ui.events.ShowPlayingViewEvent;
import de.flexguse.soundseeder.ui.events.StartPlayingEvent;
import de.flexguse.soundseeder.util.SpeakerUIConstants;

/**
 * @author Christoph Guse, info@flexguse.de
 *
 */
@SpringView(name = StoppedView.VIEW_NAME)
public class StoppedView extends SpeakerView {

	private static final long serialVersionUID = -422412905596083099L;

	public static final String VIEW_NAME = "";

	@Autowired
	private VolumeSlider volumeSlider;

	@Autowired
	private SessionEventBus sessionEventBus;

	@Autowired
	private ConfigurationService configurationService;
	
	@Override
	public void enter(ViewChangeEvent event) {
		
		super.enter(event);

		// set initial value of volume slider
		SpeakerConfiguration configuration;
		try {
			configuration = configurationService.loadConfiguration();

			if (configuration != null && configuration.getVolume() != null) {
				volumeSlider.setValue(configuration.getVolume());
			}

		} catch (ConfigurationServiceException e) {
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}

	}

	/**
	 * In this method the complete view content is initialized.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		super.afterPropertiesSet();

		// main layout
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setSizeFull();
		
		// cover / icon
		Resource iconResource = new ThemeResource("img/speaker_stopped.svg");
		Image icon = new Image();
		icon.setSource(iconResource);
		icon.setSizeFull();
		icon.addStyleName(SpeakerUIConstants.STYLE_COVER_IMAGE);
		
		// label layout
		final VerticalLayout songInfoLayout = new VerticalLayout();
		songInfoLayout.addStyleName(SpeakerUIConstants.STYLE_MUSIC_INFO_PANE);
		//songInfoLayout.setMargin(true);
		Label disconnectedInfo = new Label();
		disconnectedInfo.setValue(i18n.get("label.disconnected"));
		disconnectedInfo.addStyleName(SpeakerUIConstants.STYLE_MUSIC_TITLE);
		songInfoLayout.addComponent(disconnectedInfo);
		songInfoLayout.setComponentAlignment(disconnectedInfo, Alignment.MIDDLE_CENTER);

		VerticalLayout stopIconLayout = new VerticalLayout();
		stopIconLayout.addStyleName(SpeakerUIConstants.STYLE_MUSIC_PANE);
                stopIconLayout.setSizeFull();
		stopIconLayout.addComponent(icon);
		stopIconLayout.addComponent(songInfoLayout);
		stopIconLayout.setExpandRatio(icon, 1.0f);

		// add cover and label
		layout.addComponent(stopIconLayout);
		layout.setExpandRatio(stopIconLayout, 1.0f);
		layout.setComponentAlignment(stopIconLayout, Alignment.MIDDLE_CENTER);
		// add volume slider
		layout.addComponent(volumeSlider);
		layout.setComponentAlignment(volumeSlider, Alignment.MIDDLE_RIGHT);
		
		setContent(layout);

		createButtons();
	}

	void createButtons() {
	    // play button
	    Button connectButton = new Button(i18n.get("label.button.connect"));
	    connectButton.setIcon(FontAwesome.CHAIN);
	    connectButton.addClickListener(this::handlePlayButtonClick);

	    super.createButtons(connectButton);
	}
	
	/**
	 * This helper method handles the play button click.
	 * 
	 * @param clickEvent
	 */
	private void handlePlayButtonClick(ClickEvent clickEvent) {

		// load speaker configuration
		SpeakerConfiguration speakerConfiguration;
		try {
			speakerConfiguration = configurationService.loadConfiguration();

			// dispatch StartPlayingEvent
			sessionEventBus.publish(this,
					StartPlayingEvent.builder().eventSource(this).speakerConfiguration(speakerConfiguration).build());

			// dispatch ShowPlayingViewEvent
			applicationEventBus.publish(this, ShowPlayingViewEvent.builder().eventSource(this).build());

		} catch (ConfigurationServiceException e) {

			Notification.show(i18n.get("error.service.config.caption"),
					i18n.get("error.service.config.description", e.getMessage()), Type.ERROR_MESSAGE);

		}

	}

}
