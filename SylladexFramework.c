/******************************************************************************
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
#include "tarotDeck.h"

/*/Fetch Modus Files to include
#include "PentaFile.h" DONE
#include "NodeMap.h"
#include "timeBox.h"
#include "tarotDeck.h" DONE
*/
//create a function that can create a card item based on it's captcha code
int main(int argc, char const *argv[]) {
    char userInput[NAMESIZE + 1];

    printf("Which sylladex do you wish to load: (P)entaFile, (T)arotDeck, (N)odeMap, or Time(B)ox.\nPlease select from the single letter >> ");
    fgets(userInput, NAMESIZE, stdin);
    switch (userInput[0]) {
        case 'P':
            printf("Booting up the PentaFile Modus\n");
            PFentry();
            break;
        case 'T':
        		printf("Booting up the TarotDeck Modus\n");
        		TDentry();
            break;
        case 'N':
            break;
        case 'B':
            break;
        default:
            printf("%s\n", "unable to process what you gave.");
    }
    printf("Sylladex is now closed.\n");
    return 0;
}

/*************************** newCard *********************************/
Card newCard()
{
    //Card card = {EMPTY, "0000000", FALSE}; can only do this if i leave the array size undefined
    Card card;
    int i;
    for(i = 0; i < NAMESIZE + 1; i++) //fully initialize item
    {
        card.item[i] = 0;
    }
    strcpy(card.item, EMPTY);
    strcpy(card.captchaCode, "0000000");
    card.inUse = FALSE;
    return card;
}
