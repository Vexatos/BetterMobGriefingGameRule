/*
 * Better mobGriefing GameRule Copyright (c) 2016 Judge40
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.judge40.minecraft.bettermobgriefinggamerule.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.judge40.minecraft.bettermobgriefinggamerule.BetterMobGriefingGameRule;
import com.judge40.minecraft.bettermobgriefinggamerule.world.BetterMobGriefingGameRuleWorldSavedData;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandGameRule;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameRules;

import javax.annotation.Nullable;

/**
 *
 */
public class BetterMobGriefingGameRuleCommandGameRule extends CommandGameRule {

  /*
   * (non-Javadoc)
   * 
   * @see net.minecraft.command.CommandGameRule#processCommand(net.minecraft.command.ICommandSender,
   * java.lang.String[])
   */
  @Override
  public void execute(MinecraftServer server, ICommandSender commandSender, String[] commandWords) throws CommandException {
    if (commandWords.length >= 1 && commandWords.length <= 3
        && commandWords[0].equals(BetterMobGriefingGameRule.ORIGINAL)) {
      BetterMobGriefingGameRuleWorldSavedData worldSavedData =
          BetterMobGriefingGameRuleWorldSavedData.forWorld(commandSender.getEntityWorld());

      if (commandWords.length == 1) {

        GameRules gameRules = commandSender.getEntityWorld().getGameRules();
        String mobGriefingValue =
            gameRules.getString(BetterMobGriefingGameRule.ORIGINAL);

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder
            .append(String.format("%s = %s", BetterMobGriefingGameRule.ORIGINAL, mobGriefingValue));

        if (!worldSavedData.toString().isEmpty()) {
          messageBuilder.append(", ");
          messageBuilder.append(worldSavedData.toString());
        }

        commandSender.addChatMessage(new TextComponentString(messageBuilder.toString()));
      } else if (commandWords.length == 2) {

        if (commandWords[1].equals(Boolean.toString(true))
            || commandWords[1].equals(Boolean.toString(false))) {
          super.execute(server, commandSender, commandWords);
        } else {

          if (worldSavedData.hasMobGriefingValue(commandWords[1])) {
            String message = String.format("%s %s = %s", BetterMobGriefingGameRule.ORIGINAL,
                commandWords[1], worldSavedData.getMobGriefingValue(commandWords[1]));
            commandSender.addChatMessage(new TextComponentString(message));
          } else {
            String message =
                String.format("%s %s", BetterMobGriefingGameRule.ORIGINAL, commandWords[1]);
            notifyCommandListener(commandSender, this, "commands.gamerule.norule", message);
          }
        }

      } else if (commandWords.length == 3) {
        Class<? extends Entity> entityClass = EntityList.NAME_TO_CLASS.get(commandWords[1]);

        if (entityClass != null && EntityLiving.class.isAssignableFrom(entityClass)) {

          if (commandWords[2].equals(Boolean.toString(true))
              || commandWords[2].equals(Boolean.toString(false))
              || commandWords[2].equals(BetterMobGriefingGameRule.INHERIT)) {
            worldSavedData.setMobGriefingValue(commandWords[1], commandWords[2]);
            notifyCommandListener(commandSender, this, "commands.gamerule.success", "mobGriefing " + commandWords[1], commandWords[2]);
          } else {
            String exceptionMessage = String.format("/gamerule %s <entity name> %s|%s|%s",
                BetterMobGriefingGameRule.ORIGINAL, Boolean.toString(true), Boolean.toString(false),
                BetterMobGriefingGameRule.INHERIT);
            throw new WrongUsageException(exceptionMessage);
          }
        } else {
          throw new WrongUsageException(
              String.format("%s is not a valid entity name", commandWords[1]));
        }
      }
    } else {
      super.execute(server, commandSender, commandWords);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see net.minecraft.command.CommandGameRule#addTabCompletionOptions(net.minecraft.command.
   * ICommandSender, java.lang.String[])
   */
  @Override
  public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender commandSender, String[] commandWords, @Nullable BlockPos pos) {
    List<String> tabCompletionOptions = new ArrayList<>();

    if (commandWords.length > 1 && commandWords[0].equals(BetterMobGriefingGameRule.ORIGINAL)) {
      List<String> possibleWords = new ArrayList<>();
      possibleWords.add(Boolean.toString(true));
      possibleWords.add(Boolean.toString(false));

      if (commandWords.length == 2) {
        BetterMobGriefingGameRuleWorldSavedData betterMobGriefingGameRuleWorldSavedData =
            BetterMobGriefingGameRuleWorldSavedData.forWorld(commandSender.getEntityWorld());
        List<String> entityNames = new ArrayList<>(
            betterMobGriefingGameRuleWorldSavedData.getMobGriefingEntityNames());
        Collections.sort(entityNames);

        possibleWords.addAll(entityNames);
        tabCompletionOptions = getListOfStringsMatchingLastWord(commandWords,
            possibleWords.toArray(new String[possibleWords.size()]));
      } else if (commandWords.length == 3) {
        possibleWords.add(BetterMobGriefingGameRule.INHERIT);
        Class<? extends Entity> entityClass = EntityList.NAME_TO_CLASS.get(commandWords[1]);

        if (entityClass != null) {
          tabCompletionOptions = getListOfStringsMatchingLastWord(commandWords,
              possibleWords.toArray(new String[possibleWords.size()]));
        }
      }
    } else {
      tabCompletionOptions = super.getTabCompletionOptions(server, commandSender, commandWords, pos);
    }

    return tabCompletionOptions;
  }
}
