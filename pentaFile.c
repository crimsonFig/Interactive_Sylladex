/******************************************************************************
PentaFile PFModus by Triston Scallan
Purpose:
    A personal project that is an interactive inventory management
    datastructure for entertainment purposes, based on Homestuck webcomic
    inventory modi. Meant to be controlled through a console.
    ---
    This fetch modus is the PentaFile pfModus, a modus designed for use with a sylladex.
    Using a struct comprised of 5 arrays containing 5 cards, and busting if a
    single array is overfilled.
    This is likened to a File Cabinet. 5 folders that can hold 5 files each.
Command Parameters:
    n/a
Input:
    An optional text file to be read and load as a modus.
Results: N/A
Returns: N/A
Notes:
    1. The inventory only holds 25 cards, 5 cards in 5 folders.
    2. If 6 cards are placed into a folder, 5 are ejected and the 6th is pushed
    3. The current pfModus can have all its information saved and loaded to a txt file.
*******************************************************************************/
// If compiling using visual studio, tell the compiler not to give its warnings
// about the safety of scanf and printf
#define _CRT_SECURE_NO_WARNINGS 1

// Include files

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include "sylladexFramework.h"
#include "pentaFile.h"


//create an entry point here using the sylladex, upon entry, create an interactive
//user input loop to determine what functions the use wants to execute.
//when ready to leave, simply set the userinput While loop to FALSE so that it
//reaches the end of the entry function and returns to the sylladex.


/* A SET OF TEST AND DEBUG INSTRUCTIONS THAT GOES THROUGH ALL FUNCTIONS.
int main(int argc, char *argv[])
{
    PFModus pfModus = newPFModus();
    Card hand;
    printf("Running Modus \"PentaFile\".\n");
    PFpush(pfModus->weapons, "Dmnd Staff");
    PFpush(pfModus->weapons, "Wand");
    PFpush(pfModus->weapons, "Blade");
    PFpush(pfModus->weapons, "Marbles");
    PFdrawInventory(pfModus);
    PFpush(pfModus->misc, "Incense");
    PFpush(pfModus->info, "101101010");
    PFpush(pfModus->survival, "Gem");
    PFdrawInventory(pfModus);
    PFpush(pfModus->weapons, "SynOrb");
    PFpush(pfModus->weapons, "Lotus Blade");
    PFdrawInventory(pfModus);
    hand = PFtakeOutByName(pfModus->weapons, "Lotus Blade");
    printf("hand now holds: %s\n", hand.item);
    hand = PFtakeOutByIndex(pfModus->survival, 1);
    printf("hand now holds: %s\n", hand.item);
    PFpush(pfModus->weapons, "Blade");
    PFpush(pfModus->weapons, "Marbles");
    PFdrawInventory(pfModus);
    PFsave(pfModus);
    PFforceEjectAll(pfModus);
    PFdrawInventory(pfModus);
    pfModus = PFload();
    PFdrawInventory(pfModus);
    free(pfModus);
}*/

void PFentry()
{
    PFModus pfModus = newPFModus();
    int userQuit = FALSE;
    Card *folder;
    char itemInput[NAMESIZE + 1];
    char folderInput[NAMESIZE + 1];
    char indexInput;
    char userInput[31]; //max input should be 30ish characters
    printf("PentaFile fetch modus has successfully started.\n");
    while (!userQuit)
    {
        printf("Please input the letter of one of the following options: \n");
        printf("(l)oad, (s)ave, (a)dd a new item, retrieve by (n)ame,\n");
        printf("\tretrieve by (i)ndex, (d)isplay current inventory,\n");
        printf("(e)ject the entire modus contents, or (q)uit.\n");
        printf("==> ");
        fgets(userInput, 30, stdin);
        puts("\n");
        //AND SO BEGINS THE GREAT SWITCH LOOP

        switch (userInput[0])
        {
            case 'l':
                pfModus = PFload();
                printf("The inventory has been reverted back to a previous state of time.\n");
                break;
            case 's':
                PFsave(pfModus);
                printf("Current inventory has been 'saved'. You can reload back to this current state of time at a future point.\n");
                break;
            case 'a':
                printf("What item would you like to captalogue? >> ");
                fgets(itemInput, NAMESIZE, stdin);
                printf("What folder do you want to place into? (w,s,m,i,k) >>");
                fgets(folderInput, NAMESIZE, stdin);
                folder = findFolder(folderInput, pfModus);
                PFpush(folder, itemInput);
                break;
            case 'n':
                printf("What item would you like to retrieve? >> ");
                fgets(itemInput, NAMESIZE, stdin);
                printf("What folder do you want to pull from? (w,s,m,i,k) >>");
                fgets(folderInput, NAMESIZE, stdin);
                folder = findFolder(folderInput, pfModus);
                PFtakeOutByName(folder, itemInput);
                break;
            case 'i':
                printf("which card index do you want to retrieve from? >> ");
                indexInput = fgetc(stdin);
                printf("What folder do you want to pull from? (w,s,m,i,k) >> ");
                fgets(folderInput, NAMESIZE, stdin);
                folder = findFolder(folderInput, pfModus);
                PFtakeOutByIndex(folder, indexInput - '0'); //converts the char to its implied int and not it's ASCII value.
                break;
            case 'd':
                PFdrawInventory(pfModus);
                break;
            case 'e':
                PFforceEjectAll(pfModus);
                printf("Ejection of all inventory has been successfull.\n");
                break;
            case 'q':
                userQuit = TRUE; //the while loop will now be able to stop.
                break;
            default:
                printf("Was unable to understand what '%s' means.\n\n", userInput);
        }
    }

    //the function ends and should return back to the sylladexFramework from here.
}

/*************************** newCard *********************************/
Card newCard()
{
    //Card card = {EMPTY, "0000000", FALSE}; can only do this if i leave the array size undefined
    Card card;
    strcpy(card.item, EMPTY);
    strcpy(card.captchaCode, "0000000");
    card.inUse = FALSE;
    return card;
}
/*************************** newPFModus ******************************/
PFModus newPFModus()
{
    int i;
    Card card = newCard();
    PFModus pfModus = (PFModus) malloc(sizeof(PFModusImp));

    for (i = 0; i < 5; i++)
    {
        pfModus->weapons[i] = card;
        pfModus->survival[i] = card;
        pfModus->misc[i] = card;
        pfModus->info[i] = card;
        pfModus->keyCritical[i] = card;
    }
    return pfModus;
}

/*************************** save ***********************************
i need to follow a standardized way of saving to a file and reading from a txt
file so that all modus are compatible. i need to figure out if i should create
a seperate header file that handle this or if each modus should contain the load
and saving code for that particular modus but that it follows a standard output
format. At best, i would have to create a hybrid where the modus uses the header
file to access generalized functions to output. My current solution would to
create functions for save and load and see if i can modularize them to my given
standard, and then since they'd be a common code functions, i can put them in an
include header file and have each modus be able to utilize those functions to
process it's current set of cards. >>fgets.713 overview of c for stdin.
Currently output will be a "card entry per line"
********************************************************************/
void PFsave(PFModus pfModus)
{
    FILE *pFile;
    pFile = fopen("inventory.MSF", "wb"); //attempt to write this code as a binary instead.
    int i;
    if (pFile == NULL)
    {
        printf("%s\n", "Error in creating/opening a file to save.");
        exit(1);
    }
    //Possible save stats in this part of the code. delimit the sections somehow.
    for (i = 0; i < 5; i++)
        fwrite(&pfModus->weapons[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fwrite(&pfModus->survival[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fwrite(&pfModus->misc[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fwrite(&pfModus->info[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fwrite(&pfModus->keyCritical[i], sizeof(Card), 1, pFile);

    /* Normal Txt Save
    for (i = 0; i < 5; i++)
    {
        fprintf(pFile, "%s %s %d \n"            \
            , pfModus->weapons[i].item          \
            , pfModus->weapons[i].captchaCode   \
            , pfModus->weapons[i].inUse         );
    }
    for (i = 0; i < 5; i++)
    {
        fprintf(pFile, "%s %s %d \n"            \
            , pfModus->survival[i].item         \
            , pfModus->survival[i].captchaCode  \
            , pfModus->survival[i].inUse        );
    }
    for (i = 0; i < 5; i++)
    {
        fprintf(pFile, "%s %s %d \n"            \
            , pfModus->misc[i].item             \
            , pfModus->misc[i].captchaCode      \
            , pfModus->misc[i].inUse            );
    }
    for (i = 0; i < 5; i++)
    {
        fprintf(pFile, "%s %s %d \n"            \
            , pfModus->info[i].item             \
            , pfModus->info[i].captchaCode      \
            , pfModus->info[i].inUse            );
    }
    for (i = 0; i < 5; i++)
    {
        fprintf(pFile, "%s %s %d \n"            \
            , pfModus->keyCritical[i].item      \
            , pfModus->keyCritical[i].captchaCode   \
            , pfModus->keyCritical[i].inUse     );
    }
    */

    fclose(pFile);
}

/*************************** load ***********************************
********************************************************************/
PFModus PFload()
{
    PFModus pfModus = newPFModus();
    FILE *pFile = fopen("inventory.MSF", "rb");
    if (pFile == NULL)      //if file doesnt exist, return an empty pfModus
        return pfModus;
    int i;
    char szResponse[NAMESIZE + 1];
    //char szInputBuffer[50]; //a given card entry should only be 50 chars long
    //int iLineCount = 0;
    printf("Do you want to load (a)uto or (m)anually?: ");
    fgets(szResponse, NAMESIZE, stdin);

    if (szResponse[0] == 'a')//Automatic mode
    {
    for (i = 0; i < 5; i++)
        fread(&pfModus->weapons[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fread(&pfModus->survival[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fread(&pfModus->misc[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fread(&pfModus->info[i], sizeof(Card), 1, pFile);
    for (i = 0; i < 5; i++)
        fread(&pfModus->keyCritical[i], sizeof(Card), 1, pFile);
    }

    else if (szResponse[0] == 'm')/***** Alternative mode: Manual sort
        Loops through records and asks which folder they want the given item to
        into, then uses a switch to first assign the file data to a Card and
        then push the card to the desired folder.                           */
    {
    Card tempCard = newCard();
    while (!feof(pFile))
    {
        fread(&tempCard, sizeof(Card), 1, pFile);
        if (tempCard.inUse == FALSE) //if empty card, go to the next record
            continue;
        printf("Which folder do you want to put %s into?\n", tempCard.item);
        printf("Folders- (w)eapons, (s)urvival, (m)isc, (i)nfo, (k)eyCritical:");
        fgets(szResponse, NAMESIZE, stdin);
        switch (szResponse[0])
        {
            case 'w':
                PFpush(pfModus->weapons,tempCard.item);
                break;
            case 's':
                PFpush(pfModus->survival,tempCard.item);
                break;
            case 'm':
                PFpush(pfModus->misc,tempCard.item);
                break;
            case 'i':
                PFpush(pfModus->info,tempCard.item);
                break;
            case 'k':
                PFpush(pfModus->keyCritical,tempCard.item);
                break;
            default:
                printf("%s was not a valid option. Nothing pushed\n", szResponse);
        }
    }
    }

    else
        printf("%s\n", "Bad input. Not 'a' or 'm' was found. Empty load returned.");

    /* Txt file mode: automatic sort
    while (fgets(szInputBuffer, 50, (FILE*) pFile) != NULL)
    {
        if (iLineCount >= 0 && iLineCount < 5)
        {
            sscanf(szInputBuffer, "%s %s %d \n"                  \
                , pfModus->weapons[iLineCount % 5].item          \
                , pfModus->weapons[iLineCount % 5].captchaCode   \
                , &pfModus->weapons[iLineCount % 5].inUse        );
        }
        if (iLineCount >= 5 && iLineCount < 10)
        {
            sscanf(szInputBuffer, "%s %s %d \n"                  \
                , pfModus->survival[iLineCount % 5].item         \
                , pfModus->survival[iLineCount % 5].captchaCode  \
                , &pfModus->survival[iLineCount % 5].inUse       );
        }
        if (iLineCount >= 10 && iLineCount < 15)
        {
            sscanf(szInputBuffer, "%s %s %d \n"                  \
                , pfModus->misc[iLineCount % 5].item             \
                , pfModus->misc[iLineCount % 5].captchaCode      \
                , &pfModus->misc[iLineCount % 5].inUse           );
        }
        if (iLineCount >= 15 && iLineCount < 20)
        {
            sscanf(szInputBuffer, "%s %s %d \n"                  \
                , pfModus->info[iLineCount % 5].item             \
                , pfModus->info[iLineCount % 5].captchaCode      \
                , &pfModus->info[iLineCount % 5].inUse           );
        }
        if (iLineCount >= 20 && iLineCount < 25)
        {
            sscanf(szInputBuffer, "%s %s %d \n"                  \
                , pfModus->keyCritical[iLineCount % 5].item      \
                , pfModus->keyCritical[iLineCount % 5].captchaCode   \
                , &pfModus->keyCritical[iLineCount % 5].inUse    );
        }
        iLineCount++;
        //everything else is considered ejected. Possiby recode to follow "overfill" quirk if more than 25 cards in save file.
    }
*/

    fclose(pFile);
    return pfModus;
}

/*************************** takeOutByIndex *************************
********************************************************************/
Card PFtakeOutByIndex(Card folder[], int fileIndex)
{
    Card card = newCard();
    printf("Retrieving item from slot %d.\n", fileIndex);
    if (fileIndex < 0 || fileIndex > 5)
    {
        printf("%s\n", "requested index out of bound. Returned an EMPTY card");
        return card;
    }
    //Pass the card out of the array so that it is now empty, and return said card to user
    card = folder[fileIndex];           //set card = to specified card
    folder[fileIndex] = newCard();      //replace the card in inv with an empty card
    printf("Retrieved %s from slot %d.\n", card.item, fileIndex);
    return card;
}

/*************************** takeOutByName **************************
********************************************************************/
Card PFtakeOutByName(Card folder[], char value[])
{
    Card card = newCard();
    int bFound = FALSE;
    int i;
    printf("attempting to retrieve item: %s\n", value);
    //search for value
    for (i = 0; i < 5; i++)
    {
        if (strcmp(folder[i].item, value) == 0) //card found!
        {
            card = folder[i];
            folder[i] = newCard();
            bFound = TRUE;
            printf("%s %s\n","found the requested card: ", card.item);
            break;
        }
    }
    if (!bFound) //if not found
    {
        printf("%s\n", "Could not find requested card, Returned an EMPTY card");
        return card;
    }
    return card;
}

/*************************** push **********************************/
int PFpush(Card folder[], char item[])
{
    int i;
    Card card = newCard();
    printf("Attempting to add \"%s\" to the inventory.\n", item);
    /*if (sizeof(item) > (NAMESIZE + 1)) //need to figure out length of item[]!!!!!!!!!
    {
        printf("%s\n", "you've named said object too long, max is 12 letters.");
        return FALSE;
    }*/
    //prior prep for the soon to be pushed card
    strcpy(card.item, item);
    strcpy(card.captchaCode, "example"); //use a hash function to create a random captcha code for the item based on name.
    card.inUse = TRUE;

    //push the card to the folder
    if (PFisFull(folder)) //if folder is full, dump folder and then push the card
    {
        PFforceEject(folder);
        printf("The folder was full. Ejected all cards from folder.\n");
    }
    for (i = 0; i < 5; i++)  //search for the first empty slot and push to that, then set that slot to filled
    {
        if (folder[i].inUse) //if true, skip to the next index
            continue;
        folder[i] = card;
        printf("Added %s successfully.\n", card.item);
        return SUCCESS;
    }
    printf("%s\n", "uh oh, unable to add the item into a card for some reason.");
    return FALSE;
}

/*************************** isFull ********************************/
int PFisFull(Card folder[])
{
    int i;
    //quickly goes through the folder to test if it's used. if it finds one that is not in use, it returns FALSE. Returns true otherwise.
    for (i = 0; i < 5; i++)
    {
        if (folder[i].inUse == FALSE)
            return FALSE;
    }
    return TRUE;
}
/*************************** findFolder *********************************
Takes a string input of what the name of a folder should be. returns the
closest match for the array based on the first letter.
************************************************************************/
Card* findFolder(char szFolderName[], PFModus pfModus)
{
    Card *folder;
    switch (szFolderName[0]) {
        case 'w':
            folder = pfModus->weapons;
            break;
        case 's':
            folder = pfModus->survival;
            break;
        case 'm':
            folder = pfModus->misc;
            break;
        case 'i':
            folder = pfModus->info;
            break;
        case 'k':
            folder = pfModus->keyCritical;
            break;
    }
    return folder;
}

/*************************** forceEject ****************************/
int PFforceEject(Card card[])
{
    int i;
    Card toEject = newCard();
    for (i = 0; i < 5; i++) //dumps the entire contents of the card array/folder
    {
        toEject = card[i];
        card[i] = newCard();
        //toEject now releases the card to the world from the pfModus.
    }
    return SUCCESS;
}
/*************************** forceEjectAll ****************************/
int PFforceEjectAll(PFModus modus)
{
    int i;
    PFforceEject(modus->weapons);
    PFforceEject(modus->info);
    PFforceEject(modus->survival);
    PFforceEject(modus->keyCritical);
    PFforceEject(modus->misc);
    return SUCCESS;
}

/*************************** drawInventory *************************/
void PFdrawInventory(PFModus pfModus)
{
    int card;
    printf("\n******|Slot 1--------|Slot 2--------|Slot 3--------|Slot 4--------|Slot 5--------\n");

    printf("Weapns");
    for (card = 0; card < 5; card++)
    {
        printf("| %12s ", pfModus->weapons[card].item);
    }
    printf("|\n");

    printf("Srvivl");
    for (card = 0; card < 5; card++)
    {
        printf("| %12s ", pfModus->survival[card].item);
    }
    printf("|\n");

    printf("Misc  ");
    for (card = 0; card < 5; card++)
    {
        printf("| %12s ", pfModus->misc[card].item);
    }
    printf("|\n");

    printf("Info  ");
    for (card = 0; card < 5; card++)
    {
        printf("| %12s ", pfModus->info[card].item);
    }
    printf("|\n");

    printf("KeyCrt");
    for (card = 0; card < 5; card++)
    {
        printf("| %12s ", pfModus->keyCritical[card].item);
    }
    printf("|\n");

    printf("---------------------------------------------------------------------------------\n\n");
}
