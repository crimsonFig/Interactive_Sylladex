/********************************************************************
--- Modus by Triston Scallan
Purpose:
    A personal project that is an interactive inventory management
    datastructure for entertainment purposes, based on Homestuck webcomic
    inventory modi. Meant to be controlled through a console.
    ---
Command Parameters:
    n/a
Input:
    An optional text file to be read and load as a modus.
Results: N/A
Returns: N/A
Notes:
*********************************************************************/
// If compiling using visual studio, tell the compiler not to give its warnings
// about the safety of scanf and printf
#define _CRT_SECURE_NO_WARNINGS 1

// Include files

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include "sylladexFramework.h"
#include "tarotDeck.h"

void TDentry()
{
    freeDeck(tdModus);
}

//------------------------- initializations ------------------------

Card newCard()
{
    //Card card = {EMPTY, "0000000", FALSE}; can only do this if i leave the array size undefined
    Card card;
    strcpy(card.item, EMPTY);
    strcpy(card.captchaCode, "0000000");
    card.inUse = FALSE;
    return card;
}

TDModus newTDModus()
{
    TDModus tdModus = (TDModus) malloc(sizeof(TDModusImp));
    tdModus->deckHead = NULL        //initually EMPTY
    return tdModus;
}

//-------------------------- Save and Load -------------------------

void TDsave(TDModus tdModus)
{

}

TDModus TDload()
{

}

//-------------------------- Inventory IO --------------------------

Card TDtakeOutByIndex(TDModus deck, int fileIndex)
{

}

Card TDdrawFromTop(TDModus deck)
{
    Card card;
    Node *pRemove;
    if (isEmpty(deck))
        printf("The deck doesn't exist yet to draw from.\n");
    card = deck->deckHead->card;
    pRemove = deck->deckHead;
    deck->deckHead = deck->deckHead->pNext;
    free(pRemove);
    return card;
}

int TDcapture(TDModus deck, char item[])
{
    Node *pNew = (Node *) malloc(sizeof(NODE));
    if (pNew == NULL)
        printf("ERROR IN TRYING TO CREATE NEW CARD \n");
    pNew->card = newCard();
    strcpy(pNew->card.item, item);
    pNew->pNext = deck->deckHead;
    deck->deckHead = pNew;
}

int TDforceEjectAll(TDModus tdModus)
{

}

//-------------------------- Utility ---------------------------------

void TDdrawInventory(TDModus tdModus)
{

}

Card TDshuffle(TDModus deck)
{

}

int isEmpty(TDModus tdModus)
{
    return deck->deckHead == NULL;
}

//-------------------------- Node/Stack Functions --------------------

void freeDeck(TDModus tdModus)
{
    Node *p;
    Node *pRemove;
    for (p = tdModus->deckHead; p != NULL;)
    {
        pRemove = p;
        p = p->pNext;
        free(pRemove);
    }
    free(tdModus);
}
