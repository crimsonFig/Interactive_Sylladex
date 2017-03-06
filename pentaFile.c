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
//#include "cs2123p0.h"

/////Defined constants
#define TRUE 1
#define SUCCESS 5
#define FALSE 0
#define NAMESIZE 12
#define EMPTY "EMPTY"
/////structs and typedefs
typedef int Data;               //simply renamed
typedef struct
{
    char item[NAMESIZE + 1];    //12 for an card name. item title
    char captchaCode[8];        //7 long captcha code. Code for w/e
    int  inUse;                 //used to track if the card is empty or filled
} Card;
typedef struct
{
 //char note[64];      //personal note. NOT NEEDED.
 //Data stats[25];     //stats to be tracked go here, might output on save. might move this to the sylladex instead.
 //Data index[25];     //boolean index of the inventory, TRUE if filled, FALSE if that slot is empty. replaced by card->inUse
    //the next 5 array's can hold card of a certain kind. These are 5 arrays of the Card type, a card is a defined string.
    //consider a 2d array.
    Card weapons[5]    ;
    Card survival[5]   ;
    Card misc[5]       ;
    Card info[5]       ;
    Card keyCritical[5];
} PFModusImp;             //the actual inventory structure pfModus
typedef PFModusImp *PFModus;//a pointer to the pfModus implementation
typedef struct
{
    int isEmpty;
    Card heldItem;
} Hand;
/////Prototypes
Card newCard()                                      ;
PFModus newPFModus()                                ;
void save(PFModus pfModus)                          ;
PFModus load(char fileName[])                       ;
Card takeOutByIndex(Card folder[], int fileIndex)   ;
Card takeOutByName(Card folder[], char value[])     ;
int push(Card folder[], char item[])                ;
int isFull(Card folder[])                           ;
int forceEject(Card card[])                         ;
int forceEjectAll(PFModus modus)                    ;
void drawInventory(PFModus pfModus)                 ;
////functions

//>>"rename functions to include a pf- prefix"

/*************************** main ***********************************
********************************************************************/
int main(int argc, char *argv[])
{
    PFModus pfModus = newPFModus();
    Card hand;
    printf("Running Modus \"PentaFile\".\n");
    push(pfModus->weapons, "Dmnd Staff");
    push(pfModus->weapons, "Wand");
    push(pfModus->weapons, "Blade");
    push(pfModus->weapons, "Marbles");
    drawInventory(pfModus);
    push(pfModus->misc, "Incense");
    push(pfModus->info, "101101010");
    push(pfModus->survival, "Gem");
    drawInventory(pfModus);
    push(pfModus->weapons, "SynOrb");
    push(pfModus->weapons, "Lotus Blade");
    drawInventory(pfModus);
    hand = takeOutByName(pfModus->weapons, "Lotus Blade");
    printf("hand now holds: %s\n", hand.item);
    hand = takeOutByIndex(pfModus->survival, 1);
    printf("hand now holds: %s\n", hand.item);
    drawInventory(pfModus);
}

/*************************** newCard *********************************///done
Card newCard()
{
    //Card card = {EMPTY, "0000000", FALSE}; can only do this if i leave the array size undefined
    Card card;
    strcpy(card.item, EMPTY);
    strcpy(card.captchaCode, "0000000");
    card.inUse = FALSE;
    return card;
}
/*************************** newPFModus ******************************///done
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
********************************************************************/
void save(PFModus pfModus)
{

}

/*************************** load ***********************************
********************************************************************/
PFModus load(char fileName[])
{
PFModus pfModus;
return pfModus;
}

/*************************** takeOutByIndex *************************
********************************************************************/
Card takeOutByIndex(Card folder[], int fileIndex)
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
Card takeOutByName(Card folder[], char value[])
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

/*************************** push **********************************///done
int push(Card folder[], char item[])
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
    if (isFull(folder)) //if folder is full, dump folder and then push the card
    {
        forceEject(folder);
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

/*************************** isFull ********************************///done
int isFull(Card folder[])
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

/*************************** forceEject ****************************///done
int forceEject(Card card[])
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
/*************************** forceEjectAll ****************************///done
int forceEjectAll(PFModus modus)
{
    int i;
    forceEject(modus->weapons);
    forceEject(modus->info);
    forceEject(modus->survival);
    forceEject(modus->keyCritical);
    forceEject(modus->misc);
    return SUCCESS;
}

/*************************** drawInventory *************************///done
void drawInventory(PFModus pfModus)
{
    int card;
    printf("******|Slot 1--------|Slot 2--------|Slot 3--------|Slot 4--------|Slot 5--------\n");

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
