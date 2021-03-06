package co.uk.silvania.rpgcore.client.skillgui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import co.uk.silvania.rpgcore.RPGCore;
import co.uk.silvania.rpgcore.RPGUtils;
import co.uk.silvania.rpgcore.RegisterSkill;
import co.uk.silvania.rpgcore.network.EquipNewSkillPacket;
import co.uk.silvania.rpgcore.network.OpenGuiPacket;
import co.uk.silvania.rpgcore.skills.EquippedSkills;
import co.uk.silvania.rpgcore.skills.GlobalLevel;
import co.uk.silvania.rpgcore.skills.SkillLevelBase;
import cpw.mods.fml.client.GuiScrollingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class SkillListScrollable extends GuiScrollingList_Mod {
	
	public static final ResourceLocation skillIcons  = new ResourceLocation(RPGCore.MODID, "textures/gui/skillicons.png");
	
	int width;
	int height;
	int xSize;
	int ySize;
	
	SkillListGui parent;
	ArrayList<SkillLevelBase> skillList = RegisterSkill.skillList;
	ArrayList<SkillLevelBase> skillsForDisplay = new ArrayList<SkillLevelBase>();
	
	int selectedIndex = -1;
	Minecraft mc = Minecraft.getMinecraft();

	public SkillListScrollable(SkillListGui parent, int width, int height, int xSize, int ySize) {
		//				  w    h       h   ww  wh
		super(parent.mc, 236, 50, ((height - ySize) / 2) + 44, ((height - ySize) / 2) + 212, ((width - xSize) / 2) + 10, 44);
		//Width, height-trim top?, offset-top?, ?, widget width, widget height
		
		this.width = width;
		this.height = height;
		
		this.xSize = xSize;
		this.ySize = ySize;
		
		this.parent = parent;
		cloneSkillList();
	}
	
	private void cloneSkillList() {
		for (int i = 0; i < skillList.size(); i++) {
			skillsForDisplay.add(skillList.get(i));
		}
	}
	
	@Override
	protected int getSize() {
		for (int i = 0; i < skillsForDisplay.size(); i++) {
			SkillLevelBase skillBase = skillsForDisplay.get(i);
			SkillLevelBase skill = (SkillLevelBase) skillBase.get((EntityPlayer) mc.thePlayer, skillBase.skillId);
			if (!skill.isSkillUnlocked(mc.thePlayer) && skill.secretSkill()) {
				skillsForDisplay.remove(i);
			}
		}
		return skillsForDisplay.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		if (doubleClick) {
			RPGCore.network.sendToServer(new EquipNewSkillPacket(SkillSelectGui.slotClicked, skillsForDisplay.get(index).skillId));
			RPGCore.network.sendToServer(new OpenGuiPacket(0));
		}
		
		SkillLevelBase skillBase = skillsForDisplay.get(index);
		SkillLevelBase skill = (SkillLevelBase) skillBase.get((EntityPlayer) mc.thePlayer, skillBase.skillId);
		
		if (skill.canSkillBeEquipped(mc.thePlayer)) {
			parent.selectModIndex(index);
		}
	}

	@Override
	protected boolean isSelected(int index) {
		return this.parent.modIndexSelected(index);
	}

	@Override protected void drawBackground() {}

	Gui gui = new Gui();
	
	@Override
	protected void drawSlot(int listIndex, int width, int height, int var4, Tessellator var5) {
		GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glColor4f(1,1,1,1);
        
        SkillLevelBase skillBase = skillsForDisplay.get(listIndex);
		SkillLevelBase skill = (SkillLevelBase) skillBase.get((EntityPlayer) mc.thePlayer, skillBase.skillId);
		EquippedSkills equippedSkills = (EquippedSkills) EquippedSkills.get((EntityPlayer) mc.thePlayer);
		GlobalLevel glevel = (GlobalLevel) GlobalLevel.get((EntityPlayer) mc.thePlayer);
        
        int offset = 0;
        boolean greenFrame = false;
        boolean redFrame = false;
        
        if (!skill.canSkillBeEquipped(mc.thePlayer)) {
        	offset = 132;
        }
        
        if (!skill.isSkillCompatable(mc.thePlayer)) {
        	redFrame = true;
        }
        
        if (isSelected(listIndex)) {
        	offset = 44;
        }
        
        if (equippedSkills.isSkillEquipped(skill.skillId)) {
        	greenFrame = true;
        }
        
        mc.getTextureManager().bindTexture(skillIcons);
        gui.drawTexturedModalRect(((this.width - xSize) / 2) + 10, height, 0, 0 + offset, 236, 44);
        if (redFrame) {   gui.drawTexturedModalRect(((this.width - xSize) / 2) + 10, height, 0,  88, 236, 44); }
        if (greenFrame) { gui.drawTexturedModalRect(((this.width - xSize) / 2) + 10, height, 0, 176, 236, 44); }
		
		if (skill != null) {
			if (skill.skillIcon() != null) {
				mc.getTextureManager().bindTexture(skill.skillIcon());
				gui.drawTexturedModalRect(((this.width - xSize) / 2) + 18, height + 7, skill.iconX(), skill.iconZ(), 30, 30);
			} else {
				gui.drawTexturedModalRect(((this.width - xSize) / 2) + 18, height + 7, 0, 220, 30, 30);
			}
			
			mc.fontRenderer.drawString("Name: " + skill.nameFormat() + skill.skillName(), ((this.width - xSize) / 2) + 56, height + 9, 16777215);
			mc.fontRenderer.drawString("Lvl: " + skill.getLevel(), ((this.width - xSize) / 2) + 56, height + 18, 16777215);
			if (skill.canGainXP()) {
				mc.fontRenderer.drawString("XP: " + skill.getXPTotalForPrint(), ((this.width - xSize) / 2) + 56, height + 27, 16777215);
			}
			
			GL11.glScalef(0.5f, 0.5f, 0.5f);
			int h2 = height*2;
			int w2 = width*2;
			
			int stacker = 0; //Offset for new lines so everything always rests at the top without empty lines.
			String unlocked = skill.unlockedLevel() + "";
			if (!skill.isSkillUnlocked(mc.thePlayer) && skill.unlockedLevel() >= glevel.getLevel()) {
				mc.fontRenderer.drawString("Unlocked at Level " + skill.unlockedLevel(),  ((w2 - (xSize*2))) + 413 - (unlocked.length()*6), h2 + 9, 11796480);
				stacker += 9;
			} else if (!skill.isSkillUnlocked(mc.thePlayer)) {
				String str = "Skill locked, see details.";
				mc.fontRenderer.drawString(str,  ((w2 - (xSize*2))) + 506 - mc.fontRenderer.getStringWidth(str), h2 + 9, 11796480);
				stacker += 9;
			} else {
				if (!skill.canSkillBeEquipped(mc.thePlayer)) {
					mc.fontRenderer.drawString("Requires currently unequipped skills." ,  ((w2 - (xSize*2))) + 318, h2 + 9 + stacker, 11796480);
					stacker += 9;
				}
				if (!skill.isSkillCompatable(mc.thePlayer)) {
					mc.fontRenderer.drawString("Incompatibilities detected!" ,  ((w2 - (xSize*2))) + 379, h2 + 9 + stacker, 11796480);
					stacker += 9;
				}
				if (stacker > 0) {
					mc.fontRenderer.drawString("Hover here for more info." ,  ((w2 - (xSize*2))) + 374, h2 + 18 + stacker, 11796480);
				} else if (equippedSkills.isSkillEquipped(skill.skillId)) {
					mc.fontRenderer.drawString("Skill Equipped!", ((w2 - (xSize*2))) + 437, h2 + 9, 25600);
				}
				
			}
			GL11.glScalef(2.0f, 2.0f, 2.0f);

		}
	}
}
