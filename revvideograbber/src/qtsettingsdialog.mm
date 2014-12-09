/* Copyright (C) 2003-2013 Runtime Revolution Ltd.

This file is part of LiveCode.

LiveCode is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License v3 as published by the Free
Software Foundation.

LiveCode is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License
along with LiveCode.  If not see <http://www.gnu.org/licenses/>.  */

#include <Cocoa/Cocoa.h>
#include <QuickTime/QuickTime.h>
#include "core.h"


@interface com_runrev_livecode_MCAudioSettingsPanel: NSWindow
{
    NSButton *m_ok_button;
    NSButton *m_settings_button;
    
    NSPopUpButton *m_input_list_popup;
    NSButton *m_mute_checkbox;
    
    SGChannel m_channel;
    
    bool m_finished;
}

-(id) initWithChannel: (SGChannel *) p_channel;
-(void) dealloc;
-(void) windowWillClose: (NSNotification *)notification;
-(void) windowDidResize:(NSNotification *)notification;

-(bool) setSource: (NSInteger) index;
-(void) openSettings;

//-(void) changeColor:(id)sender;
-(void) pickerSettingsClicked;
-(void) pickerSourceSelected;
-(void) pickerOkClicked;
-(void) relayout;

@end

@implementation com_runrev_livecode_MCAudioSettingsPanel

-(id) initWithChannel: (SGChannel *) p_channel
{
    NSRect t_rect =  { { 600,600 }, {1000, 1000}};
    self = [super initWithContentRect: t_rect styleMask: NSBorderlessWindowMask backing: NSBackingStoreBuffered defer: NO];

    m_channel = *p_channel;
    
    m_finished = false;
    
    // Create the buttons
    NSRect t_ok_button_rect =  { { 600,600 }, {100, 100}};
    NSRect t_settings_button_rect =  { { 400,600 }, {100, 100}};
    NSRect t_mute_checkbox_rect =  { { 200,600 }, {100, 100}};
    NSRect t_input_list_popup_rect =  { { 0,600 }, {100, 100}};
    
    m_ok_button = [[NSButton alloc] initWithFrame:t_ok_button_rect];
    m_settings_button = [[NSButton alloc] initWithFrame:t_settings_button_rect];
    m_mute_checkbox = [[NSButton alloc] initWithFrame:t_mute_checkbox_rect];
    m_input_list_popup = [[NSPopUpButton alloc] initWithFrame:t_input_list_popup_rect];
    
    m_ok_button.bezelStyle = NSRoundedBezelStyle;
    m_ok_button.imagePosition = NSNoImage;
    [m_ok_button setTitle: @"OK"];
    [m_ok_button setAction:@selector(pickerOkClicked)];
    [m_ok_button setTarget:self];
    
    m_settings_button.bezelStyle = NSRoundedBezelStyle;
    m_settings_button.imagePosition = NSNoImage;
    [m_settings_button setTitle: @"Channel Settings"];
    [m_settings_button setAction:@selector(pickerSettingsClicked)];
    [m_settings_button setTarget:self];
    
    NSString *t_input_source = nil;
    
    NSArray *t_input_list = nil;
    QTGetComponentProperty(m_channel, kQTPropertyClass_SGAudioRecordDevice, kQTSGAudioPropertyID_InputListWithAttributes, sizeof(t_input_list), &t_input_list, NULL);
    
    if (t_input_list)
    {
        for (int i = 0; i < [t_input_list count]; i++)
        {
            NSDictionary *t_dict = [t_input_list objectAtIndex:i++];
            t_input_source = [t_dict objectForKey:(id)kQTAudioDeviceAttribute_DeviceInputDescription];
            
            [m_input_list_popup addItemWithTitle:t_input_source];
        }
    }

    [m_input_list_popup setTitle: @"Input Source"];
    [m_input_list_popup setAction:@selector(pickerSourceClicked)];
    [m_input_list_popup setTarget:self];
    
    [[self contentView] addSubview: m_ok_button];
    [[self contentView] addSubview: m_settings_button];
    [[self contentView] addSubview: m_mute_checkbox];
    [[self contentView] addSubview: m_input_list_popup];
    
    [self relayout];
    
    return self;
}

-(void)dealloc
{
    [m_ok_button removeFromSuperview];
    [m_ok_button release];
    
    [m_settings_button removeFromSuperview];
    [m_settings_button release];
    
    [m_input_list_popup removeFromSuperview];
    [m_input_list_popup release];
    
    [m_mute_checkbox removeFromSuperview];
    [m_mute_checkbox release];
    
    [super dealloc];
}

-(void)relayout
{
    [m_ok_button setButtonType: NSMomentaryLightButton];

    [m_ok_button setNeedsDisplay:YES];
    
    [m_settings_button setButtonType: NSMomentaryLightButton];

    [m_settings_button setNeedsDisplay:YES];

    [m_input_list_popup setNeedsDisplay:YES];
}

//////////

- (bool) setSource: (NSInteger)index
{
    NSArray *t_input_list = nil;
    // TODO: Throws an exception
    QTGetComponentProperty(m_channel, kQTPropertyClass_SGAudioRecordDevice, kQTSGAudioPropertyID_InputListWithAttributes, sizeof(t_input_list), &t_input_list, NULL);
    
    if (t_input_list)
    {
        NSLog(@"Count is ================= %d", t_input_list.count);
        NSLog(@"Index is ================= %d", index);
        // TODO An uncaught exception was raised -[__NSArrayM objectAtIndex:]: index 1 beyond bounds [0 .. 0]

        NSDictionary *t_dict = [t_input_list objectAtIndex:index];
        unsigned int t_id = [(NSNumber*)[t_dict objectForKey:(id)kQTAudioDeviceAttribute_DeviceInputID] unsignedIntValue];
        
        return noErr == QTSetComponentProperty(m_channel, kQTPropertyClass_SGAudioRecordDevice, kQTSGAudioPropertyID_InputSelection, sizeof(t_id), &t_id);
    }
    
}

- (void) openSettings
{
    OSErr err;
    err = noErr;
    
    ComponentInstance ci;
    OpenADefaultComponent(StandardCompressionType, StandardCompressionSubTypeAudio, &ci);
    
    AudioStreamBasicDescription t_description;
    
    AudioChannelLayoutTag t_layout_tags[] =
    {
        kAudioChannelLayoutTag_UseChannelDescriptions,
        kAudioChannelLayoutTag_Mono,
        kAudioChannelLayoutTag_Stereo,
    };
    
    QTSetComponentProperty(ci, kQTPropertyClass_SCAudio, kQTSCAudioPropertyID_ClientRestrictedChannelLayoutTagList,sizeof(t_layout_tags), t_layout_tags);
    
    if (err == noErr)
        err = SCRequestImageSettings(ci);
    
    if (err == noErr)
        err = QTGetComponentProperty(ci, kQTPropertyClass_SCAudio,kQTSCAudioPropertyID_BasicDescription,sizeof(t_description), &t_description, NULL);
    
    if (err == noErr)
    {
        CFArrayRef t_codec_settings = nil;
        void *t_magic_cookie = nil;
        UInt32 t_magic_cookie_size = 0;
        
        QTGetComponentProperty(ci, kQTPropertyClass_SCAudio,
                               kQTSCAudioPropertyID_CodecSpecificSettingsArray,
                               sizeof(t_codec_settings), &t_codec_settings, NULL);
        
        if (!t_codec_settings &&
            (noErr == QTGetComponentPropertyInfo(ci, kQTPropertyClass_SCAudio,
                                                 kQTSCAudioPropertyID_MagicCookie,
                                                 NULL, &t_magic_cookie_size, NULL)) && t_magic_cookie_size)
        {
            MCMemoryAllocate(t_magic_cookie_size, t_magic_cookie);
            QTGetComponentProperty(ci, kQTPropertyClass_SCAudio,
                                   kQTSCAudioPropertyID_MagicCookie,
                                   t_magic_cookie_size, t_magic_cookie, &t_magic_cookie_size);
        }
        
        if (err == noErr)
            err = QTSetComponentProperty(m_channel, kQTPropertyClass_SGAudio, kQTSGAudioPropertyID_StreamFormat, sizeof(t_description), &t_description);
        
        // Set any additional settings for this configuration
        if (t_magic_cookie_size != 0)
            QTSetComponentProperty(m_channel, kQTPropertyClass_SCAudio, kQTSCAudioPropertyID_MagicCookie,
                                   t_magic_cookie_size, t_magic_cookie);
        else if (t_codec_settings)
            QTSetComponentProperty(m_channel, kQTPropertyClass_SCAudio, kQTSCAudioPropertyID_CodecSpecificSettingsArray,
                                   sizeof(t_codec_settings), t_codec_settings);
    }
}

- (void) mutePreview
{
    SGStop(m_channel);
}

- (void) unmutePreview
{
    SGStop(m_channel);
    SGStartPreview(m_channel);
}

//////////

// NSWindow delegate's method
- (void)windowDidResize:(NSNotification *)notification
{
    [self relayout];
}

-(void) windowWillClose:(NSNotification *)notification
{
    m_finished = true;
}

//////////
// Selectors called when the according button is pressed.

-(void) pickerOkClicked
{
    m_finished = true;
    
    [self close];
}

-(void) pickerSourceClicked
{
    int index = [m_input_list_popup indexOfSelectedItem];
    [self setSource:index];
}

-(void) pickerSettingsClicked
{
    [self openSettings];
}

//////////

-(bool) result
{
    return m_finished;
}

@end

////////////////////////////////////////////////////////////////////////////////

void CQTVideoGrabberOpenDialog(SGChannel *channel)
{
    //[NSApp runModalForWindow: [[com_runrev_livecode_MCAudioSettingsPanel alloc] initWithChannel:channel]];
    NSWindow *t_window = [[com_runrev_livecode_MCAudioSettingsPanel alloc] initWithChannel:channel];
    [t_window setBackgroundColor:[NSColor blueColor]];
    [t_window makeKeyAndOrderFront:NSApp];
    //[NSApp runModalForWindow: t_window];
    //[t_window makeKeyAndOrderFront:t_window];
    
}

////////////////////////////////////////////////////////////////////////////////
